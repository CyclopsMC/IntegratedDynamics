package org.cyclops.integrateddynamics.core.network;

import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.api.part.PartPos;

import javax.annotation.Nonnull;
import java.util.Iterator;

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
        Iterator<PartPos> it = this.getNonFullPositions();
        while (it.hasNext()) {
            PartPos pos = it.next();
            this.network.disablePosition(pos);
            ingredient = this.network.getPositionedStorage(pos).insert(ingredient, simulate);
            this.network.enablePosition(pos);
            if (matcher.isEmpty(ingredient)) {
                break;
            }
        }

        // Re-add skipped quantity to response if applicable
        if (skippedQuantity > 0) {
            ingredient = matcher.withQuantity(ingredient, skippedQuantity + matcher.getQuantity(ingredient));
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
        Iterator<PartPos> it = this.getNonEmptyPositions();
        while (it.hasNext()) {
            PartPos pos = it.next();
            this.network.disablePosition(pos);
            T extracted = this.network.getPositionedStorage(pos).extract(maxQuantity, simulate);
            this.network.enablePosition(pos);
            if (!matcher.isEmpty(extracted)) {
                return extracted;
            }
        }

        return matcher.getEmptyInstance();
    }

    @Override
    public T extract(@Nonnull T prototype, M matchFlags, boolean simulate) {
        IIngredientMatcher<T, M> matcher = getComponent().getMatcher();

        // Limit rate
        long limit = network.getRateLimit();
        if (matcher.getQuantity(prototype) > limit) {
            prototype = matcher.withQuantity(prototype, limit);
        }

        // Try extracting from all positions that match with the given conditions
        // until one succeeds.
        Iterator<PartPos> it = this.getMatchingPositions(prototype, matchFlags);
        while (it.hasNext()) {
            PartPos pos = it.next();
            this.network.disablePosition(pos);
            T extracted = this.network.getPositionedStorage(pos).extract(prototype, matchFlags, simulate);
            this.network.enablePosition(pos);
            if (!matcher.isEmpty(extracted)) {
                return extracted;
            }
        }

        return matcher.getEmptyInstance();
    }
}
