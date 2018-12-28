package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.cyclopscore.ingredient.collection.IIngredientMapMutable;
import org.cyclops.cyclopscore.ingredient.collection.IngredientHashMap;
import org.cyclops.integrateddynamics.api.network.IPartPosIteratorHandler;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.api.part.PartPos;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * An abstract {@link IIngredientComponentStorage} that wraps over a {@link IPositionedAddonsNetworkIngredients}.
 *
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 */
public abstract class IngredientChannelAdapter<T, M> implements IIngredientComponentStorage<T, M> {

    private final IPositionedAddonsNetworkIngredients<T, M> network;
    private final int channel;

    public IngredientChannelAdapter(PositionedAddonsNetworkIngredients<T, M> network, int channel) {
        this.network = network;
        this.channel = channel;
    }

    public IPositionedAddonsNetworkIngredients<T, M> getNetwork() {
        return network;
    }

    public int getChannel() {
        return channel;
    }

    @Override
    public IngredientComponent<T, M> getComponent() {
        return network.getComponent();
    }

    protected abstract Iterator<PartPos> getNonFullPositions();
    protected abstract Iterator<PartPos> getAllPositions();
    protected abstract Iterator<PartPos> getNonEmptyPositions();
    protected abstract Iterator<PartPos> getMatchingPositions(@Nonnull T prototype, M matchFlags);

    @Override
    public long getMaxQuantity() {
        long sum = 0;
        Iterator<PartPos> it = getAllPositions();
        while (it.hasNext()) {
            sum = Math.addExact(sum, this.network.getPositionedStorage(it.next()).getMaxQuantity());
        }
        return sum;
    }

    protected Pair<IPartPosIteratorHandler, Iterator<PartPos>> getPartPosIteratorData(Supplier<Iterator<PartPos>> iteratorSupplier, int channel) {
        IPartPosIteratorHandler handler = network.getPartPosIteratorHandler();
        if (handler == null) {
            handler = PartPosIteratorHandlerDummy.INSTANCE;
        } else {
            handler = handler.clone();
        }
        return Pair.of(handler, handler.handleIterator(iteratorSupplier, channel));
    }

    protected void savePartPosIteratorHandler(IPartPosIteratorHandler partPosIteratorHandler) {
        network.setPartPosIteratorHandler(partPosIteratorHandler);
    }

    @Override
    public T insert(@Nonnull T ingredient, boolean simulate) {
        IIngredientMatcher<T, M> matcher = getComponent().getMatcher();

        // Quickly return if the to-be-inserted ingredient was already empty
        if (matcher.isEmpty(ingredient)) {
            return ingredient;
        }

        // Limit rate
        long limit = network.getRateLimit();
        long currentQuantity = matcher.getQuantity(ingredient);
        long skippedQuantity = 0;
        if (currentQuantity > limit) {
            ingredient = matcher.withQuantity(ingredient, limit);
            skippedQuantity = currentQuantity - limit;
        }

        // Try inserting the ingredient at all positions that are not full,
        // until the ingredient becomes completely empty.
        Pair<IPartPosIteratorHandler, Iterator<PartPos>> partPosIteratorData = getPartPosIteratorData(this::getNonFullPositions, channel);
        Iterator<PartPos> it = partPosIteratorData.getRight();
        while (it.hasNext()) {
            PartPos pos = it.next();
            this.network.disablePosition(pos);
            long quantityBefore = matcher.getQuantity(ingredient);
            ingredient = this.network.getPositionedStorage(pos).insert(ingredient, simulate);
            long quantityAfter = matcher.getQuantity(ingredient);
            this.network.enablePosition(pos);
            if (!simulate && quantityBefore != quantityAfter) {
                this.network.scheduleObservationForced(channel, pos); // Mark the position as 'changed'
            }
            if (matcher.isEmpty(ingredient)) {
                break;
            }
        }

        // Re-add skipped quantity to response if applicable
        if (skippedQuantity > 0) {
            ingredient = matcher.withQuantity(ingredient, skippedQuantity + matcher.getQuantity(ingredient));
        }

        if (!simulate) {
            savePartPosIteratorHandler(partPosIteratorData.getLeft());
        }

        return ingredient;
    }

    @Override
    public T extract(long maxQuantity, boolean simulate) {
        IIngredientMatcher<T, M> matcher = getComponent().getMatcher();

        // Limit rate
        maxQuantity = (int) Math.min(maxQuantity, network.getRateLimit());

        // Try extracting from all non-empty positions
        // until one succeeds.
        Pair<IPartPosIteratorHandler, Iterator<PartPos>> partPosIteratorData = getPartPosIteratorData(this::getNonEmptyPositions, channel);
        Iterator<PartPos> it = partPosIteratorData.getRight();
        while (it.hasNext()) {
            PartPos pos = it.next();
            this.network.disablePosition(pos);
            T extracted = this.network.getPositionedStorage(pos).extract(maxQuantity, simulate);
            if (!this.canExtract(extracted)) {
                extracted = matcher.getEmptyInstance();
            }
            this.network.enablePosition(pos);
            if (!matcher.isEmpty(extracted)) {
                if (!simulate) {
                    this.network.scheduleObservationForced(channel, pos); // Mark the position as 'changed'
                    savePartPosIteratorHandler(partPosIteratorData.getLeft());
                }
                return extracted;
            }
        }

        if (!simulate) {
            savePartPosIteratorHandler(partPosIteratorData.getLeft());
        }

        return matcher.getEmptyInstance();
    }

    @Override
    public T extract(@Nonnull T prototype, M matchFlags, boolean simulate) {
        IIngredientMatcher<T, M> matcher = getComponent().getMatcher();
        boolean checkQuantity = matcher.hasCondition(matchFlags, getComponent().getPrimaryQuantifier().getMatchCondition());

        // Limit rate
        long limit = network.getRateLimit();
        if (matcher.getQuantity(prototype) > limit) {
            // Fail immediately if we require more than the limit
            if (checkQuantity) {
                return matcher.getEmptyInstance();
            }

            // Otherwise, we reduce our requested quantity
            prototype = matcher.withQuantity(prototype, limit);
        }
        final T prototypeFinal = prototype;
        long requiredQuantity = matcher.getQuantity(prototypeFinal);

        // Modify our match condition that will be used to test each separate interface
        if (checkQuantity) {
            matchFlags = matcher.withoutCondition(matchFlags, getComponent().getPrimaryQuantifier().getMatchCondition());
        }
        M finalMatchFlags = matchFlags;

        // Maintain a temporary mapping of prototype items to their total count over all positions,
        // plus the list of positions in which they are present.
        IIngredientMapMutable<T, M, Pair<Wrapper<Long>, List<PartPos>>> validInstancesCollapsed = new IngredientHashMap<>(getComponent());

        // Try extracting from all positions that match with the given conditions
        // until one succeeds.
        Pair<IPartPosIteratorHandler, Iterator<PartPos>> partPosIteratorData = getPartPosIteratorData(() -> this.getMatchingPositions(prototypeFinal, finalMatchFlags), channel);
        Iterator<PartPos> it = partPosIteratorData.getRight();
        while (it.hasNext()) {
            PartPos pos = it.next();

            // Do a simulated extraction
            this.network.disablePosition(pos);
            T extractedSimulated = this.network.getPositionedStorage(pos).extract(prototypeFinal, finalMatchFlags, true);
            if (!this.canExtract(extractedSimulated)) {
                extractedSimulated = matcher.getEmptyInstance();
            }
            this.network.enablePosition(pos);
            T storagePrototype = getComponent().getMatcher().withQuantity(extractedSimulated, 1);

            // Get existing value from temporary mapping
            Pair<Wrapper<Long>, List<PartPos>> existingValue = validInstancesCollapsed.get(storagePrototype);
            if (existingValue == null) {
                existingValue = Pair.of(new Wrapper<>(0L), Lists.newLinkedList());
                validInstancesCollapsed.put(storagePrototype, existingValue);
            }

            // Update the counter and pos-list for our prototype
            long newCount = existingValue.getLeft().get() + matcher.getQuantity(extractedSimulated);
            existingValue.getLeft().set(newCount);
            existingValue.getRight().add(pos);

            // If the count is sufficient for our query, return
            if (newCount >= requiredQuantity) {
                // Save the iterator state before returning
                if (!simulate) {
                    savePartPosIteratorHandler(partPosIteratorData.getLeft());
                }
                existingValue.getLeft().set(requiredQuantity);
                return finalizeExtraction(storagePrototype, matchFlags, existingValue, requiredQuantity, simulate);
            }
        }

        // If we reach this point, then our effective count is below requiredQuantity

        // Save the iterator state before returning
        if (!simulate) {
            savePartPosIteratorHandler(partPosIteratorData.getLeft());
        }

        // Fail if we required an exact quantity
        if (checkQuantity) {
            return matcher.getEmptyInstance();
        }

        // Extract for the instance that had the most matches if we didn't require an exact quantity
        Pair<Wrapper<Long>, List<PartPos>> maxValue = Pair.of(new Wrapper<>(0L), Lists.newArrayList());
        T maxInstance = matcher.getEmptyInstance();
        for (Map.Entry<T, Pair<Wrapper<Long>, List<PartPos>>> entry : validInstancesCollapsed) {
            if (entry.getValue().getLeft().get() > maxValue.getLeft().get()) {
                maxInstance = entry.getKey();
                maxValue = entry.getValue();
            }
        }
        return finalizeExtraction(maxInstance, matchFlags, maxValue, requiredQuantity, simulate);
    }

    /**
     * Check if the given instance can be extracted.
     *
     * This is needed in cases where you want to block the extraction
     * if it has not yet been indexed properly.
     * Otherwise, changes to a storage may not be indexed at all,
     * and important information may be lost.
     *
     * @param extractedSimulated A simulated extraction.
     * @return If the extraction is allowed.
     */
    protected boolean canExtract(T extractedSimulated) {
        return true;
    }

    protected T finalizeExtraction(T instancePrototype, M matchFlags, Pair<Wrapper<Long>, List<PartPos>> value,
                                   long requiredQuantity, boolean simulate) {
        IIngredientMatcher<T, M> matcher = getComponent().getMatcher();
        long extractedCount = value.getLeft().get();
        instancePrototype = matcher.withQuantity(instancePrototype, extractedCount);
        if (!simulate && extractedCount > 0) {
            long toExtract = requiredQuantity;
            for (PartPos pos : value.getRight()) {
                this.network.disablePosition(pos);
                T extracted = this.network.getPositionedStorage(pos).extract(instancePrototype, matchFlags, false);
                this.network.enablePosition(pos);
                this.network.scheduleObservationForced(channel, pos); // Mark the position as 'changed'
                toExtract -= matcher.getQuantity(extracted);
            }
            // Quick heuristic check to see if 'storage' did not lie during its simulation
            if (toExtract != requiredQuantity - extractedCount) {
                throw new IllegalStateException("A storage resulted in inconsistent simulated and non-simulated output.");
            }
        }
        return getComponent().getMatcher().withQuantity(instancePrototype, extractedCount);
    }

}
