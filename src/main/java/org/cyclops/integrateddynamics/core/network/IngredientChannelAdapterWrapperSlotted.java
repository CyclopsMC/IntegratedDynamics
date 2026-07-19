package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorageSlotted;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.api.network.PositionedAddonsNetworkIngredientsFilter;
import org.cyclops.integrateddynamics.api.part.PartPos;

import javax.annotation.Nonnull;
import java.util.Iterator;

/**
 * A slotted wrapper over {@link IngredientChannelAdapter}.
 *
 * It exposes slots by chaining storages sequentially, not using the index.
 *
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 *
 * @author rubensworks
 */
public class IngredientChannelAdapterWrapperSlotted<T, M> implements IIngredientComponentStorageSlotted<T, M> {

    private final IngredientChannelAdapter<T, M> channel;
    private final Int2IntMap cacheChannelSlots;

    public IngredientChannelAdapterWrapperSlotted(IngredientChannelAdapter<T, M> channel, Int2IntMap cacheChannelSlots) {
        this.channel = channel;
        this.cacheChannelSlots = cacheChannelSlots;
    }

    protected static int getIngredientComponentStorageSize(IIngredientComponentStorage<?, ?> storage) {
        if (storage instanceof IIngredientComponentStorageSlotted) {
            return ((IIngredientComponentStorageSlotted<?, ?>) storage).getSlots();
        } else {
            return Iterators.size(storage.iterator()) + 1;
        }
    }

    @Override
    public int getSlots() {
        int slots = this.cacheChannelSlots.getOrDefault(this.channel.getChannel(), -1);
        if (slots != -1) {
            return slots;
        }

        slots = 0;
        IPositionedAddonsNetworkIngredients<T, M> network = this.channel.getNetwork();

        boolean hasDisabledPosition = false;
        for (PartPos pos : network.getPositions(this.channel.getChannel())) {
            // Skip if the position is not loaded or disabled
            if (!pos.getPos().isLoaded()) {
                continue;
            }
            if (network.isPositionDisabled(pos)) {
                hasDisabledPosition = true;
                continue;
            }
            network.disablePosition(pos);
            IIngredientComponentStorage<T, M> storage = network.getPositionedStorage(pos);
            slots = IModHelpers.get().getBaseHelpers().addSafe(slots, getIngredientComponentStorageSize(storage));
            network.enablePosition(pos);
        }

        if (!hasDisabledPosition) {
            this.cacheChannelSlots.put(this.channel.getChannel(), slots);
        }
        return slots;
    }

    protected Triple<IIngredientComponentStorage<T, M>, Integer, PartPos> getStorageAndRelativeSlot(int slot) {
        IPositionedAddonsNetworkIngredients<T, M> network = this.channel.getNetwork();

        for (PartPos pos : network.getPositions(this.channel.getChannel())) {
            // Skip if the position is not loaded or disabled
            if (!pos.getPos().isLoaded() || network.isPositionDisabled(pos)) {
                continue;
            }
            network.disablePosition(pos);
            IIngredientComponentStorage<T, M> storage = network.getPositionedStorage(pos);
            int storageSize = getIngredientComponentStorageSize(storage);
            network.enablePosition(pos);
            if (slot < storageSize) {
                return Triple.of(storage, slot, pos);
            } else {
                slot -= storageSize;
            }
        }

        return Triple.of(null, -1, null);
    }

    @Override
    public T getSlotContents(int slotAbsolute) {
        Triple<IIngredientComponentStorage<T, M>, Integer, PartPos> storageAndSlot = getStorageAndRelativeSlot(slotAbsolute);
        IIngredientComponentStorage<T, M> storage = storageAndSlot.getLeft();
        int slotRelative = storageAndSlot.getMiddle();
        PartPos pos = storageAndSlot.getRight();
        if (storage == null) {
            return getComponent().getMatcher().getEmptyInstance();
        }

        if (storage instanceof IIngredientComponentStorageSlotted) {
            return ((IIngredientComponentStorageSlotted<T, M>) storage).getSlotContents(slotRelative);
        } else {
            try {
                T ingredient = Iterators.get(storage.iterator(), slotRelative);
                PositionedAddonsNetworkIngredientsFilter<T> filter = this.channel.getNetwork().getPositionedStorageFilter(pos);
                if (filter != null && !filter.testView(ingredient)) {
                    return getComponent().getMatcher().getEmptyInstance();
                }
                return ingredient;
            } catch (IndexOutOfBoundsException e) {
                return getComponent().getMatcher().getEmptyInstance();
            }
        }
    }

    @Override
    public long getMaxQuantity(int slotAbsolute) {
        Triple<IIngredientComponentStorage<T, M>, Integer, PartPos> storageAndSlot = getStorageAndRelativeSlot(slotAbsolute);
        IIngredientComponentStorage<T, M> storage = storageAndSlot.getLeft();
        int slotRelative = storageAndSlot.getMiddle();
        if (storage == null) {
            return 0;
        }

        if (storage instanceof IIngredientComponentStorageSlotted) {
            return ((IIngredientComponentStorageSlotted<T, M>) storage).getMaxQuantity(slotRelative);
        } else {
            return IModHelpers.get().getBaseHelpers().castSafe(getComponent().getMatcher().getMaximumQuantity());
        }
    }

    @Override
    public T insert(int slotAbsolute, @Nonnull T ingredient, TransactionContext transaction) {
        // First run the ingredient instance through the pre-consumers.
        for (IIngredientChannelInsertPreConsumer<T> insertPreConsumer : this.channel.getNetwork().getInsertPreConsumers()) {
            ingredient = insertPreConsumer.insert(this.channel.getChannel(), ingredient, transaction);
        }

        Triple<IIngredientComponentStorage<T, M>, Integer, PartPos> storageAndSlot = getStorageAndRelativeSlot(slotAbsolute);
        IIngredientComponentStorage<T, M> storage = storageAndSlot.getLeft();
        int slotRelative = storageAndSlot.getMiddle();
        PartPos pos = storageAndSlot.getRight();
        if (storage == null) {
            return ingredient;
        }

        PositionedAddonsNetworkIngredientsFilter<T> filter = this.channel.getNetwork().getPositionedStorageFilter(pos);
        if (filter != null && !filter.testInsertion(ingredient)) {
            return ingredient;
        }

        if (storage instanceof IIngredientComponentStorageSlotted) {
            return ((IIngredientComponentStorageSlotted<T, M>) storage).insert(slotRelative, ingredient, transaction);
        } else {
            return storage.insert(ingredient, transaction);
        }
    }

    @Override
    public T extract(int slotAbsolute, long maxQuantity, TransactionContext transaction) {
        Triple<IIngredientComponentStorage<T, M>, Integer, PartPos> storageAndSlot = getStorageAndRelativeSlot(slotAbsolute);
        IIngredientComponentStorage<T, M> storage = storageAndSlot.getLeft();
        int slotRelative = storageAndSlot.getMiddle();
        PartPos pos = storageAndSlot.getRight();
        if (storage == null) {
            return getComponent().getMatcher().getEmptyInstance();
        }

        // If we do an effective extraction, first simulate to check if it matches the filter
        PositionedAddonsNetworkIngredientsFilter<T> filter = this.channel.getNetwork().getPositionedStorageFilter(pos);
        if (filter != null) {
            try (var tx = Transaction.open(transaction)) {
                T extractedSimulated;
                if (storage instanceof IIngredientComponentStorageSlotted) {
                    extractedSimulated = ((IIngredientComponentStorageSlotted<T, M>) storage).extract(slotRelative, maxQuantity, tx);
                } else {
                    extractedSimulated = storage.extract(maxQuantity, tx);
                }
                if (!filter.testExtraction(extractedSimulated)) {
                    return getComponent().getMatcher().getEmptyInstance();
                }
            }
        }

        T extracted;
        if (storage instanceof IIngredientComponentStorageSlotted) {
            extracted = ((IIngredientComponentStorageSlotted<T, M>) storage).extract(slotRelative, maxQuantity, transaction);
        } else {
            extracted = storage.extract(maxQuantity, transaction);
        }

        return extracted;
    }

    @Override
    public IngredientComponent<T, M> getComponent() {
        return channel.getComponent();
    }

    @Override
    public Iterator<T> iterator() {
        return channel.iterator();
    }

    @Override
    public Iterator<T> iterator(@Nonnull T prototype, M matchCondition) {
        return channel.iterator(prototype, matchCondition);
    }

    @Override
    public long getMaxQuantity() {
        return channel.getMaxQuantity();
    }

    @Override
    public T insert(@Nonnull T ingredient, TransactionContext transaction) {
        return channel.insert(ingredient, transaction);
    }

    @Override
    public T extract(@Nonnull T prototype, M matchCondition, TransactionContext transaction) {
        return channel.extract(prototype, matchCondition, transaction);
    }

    @Override
    public T extract(long maxQuantity, TransactionContext transaction) {
        return channel.extract(maxQuantity, transaction);
    }
}
