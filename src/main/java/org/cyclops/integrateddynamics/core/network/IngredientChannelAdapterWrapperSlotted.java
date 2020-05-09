package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.Iterators;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorageSlotted;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
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

    public IngredientChannelAdapterWrapperSlotted(IngredientChannelAdapter<T, M> channel) {
        this.channel = channel;
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
        int slots = 0;
        IPositionedAddonsNetworkIngredients<T, M> network = this.channel.getNetwork();

        for (PartPos pos : network.getPositions()) {
            // Skip if the position is not loaded or disabled
            if (!pos.getPos().isLoaded() || network.isPositionDisabled(pos)) {
                continue;
            }
            network.disablePosition(pos);
            IIngredientComponentStorage<T, M> storage = network.getPositionedStorage(pos);
            slots = Helpers.addSafe(slots, getIngredientComponentStorageSize(storage));
            network.enablePosition(pos);
        }

        return slots;
    }

    protected Pair<IIngredientComponentStorage<T, M>, Integer> getStorageAndRelativeSlot(int slot) {
        IPositionedAddonsNetworkIngredients<T, M> network = this.channel.getNetwork();

        for (PartPos pos : network.getPositions()) {
            // Skip if the position is not loaded or disabled
            if (!pos.getPos().isLoaded() || network.isPositionDisabled(pos)) {
                continue;
            }
            network.disablePosition(pos);
            IIngredientComponentStorage<T, M> storage = network.getPositionedStorage(pos);
            int storageSize = getIngredientComponentStorageSize(storage);
            network.enablePosition(pos);
            if (slot < storageSize) {
                return Pair.of(storage, slot);
            } else {
                slot -= storageSize;
            }
        }

        return Pair.of(null, -1);
    }

    @Override
    public T getSlotContents(int slotAbsolute) {
        Pair<IIngredientComponentStorage<T, M>, Integer> storageAndSlot = getStorageAndRelativeSlot(slotAbsolute);
        IIngredientComponentStorage<T, M> storage = storageAndSlot.getLeft();
        int slotRelative = storageAndSlot.getRight();
        if (storage == null) {
            return getComponent().getMatcher().getEmptyInstance();
        }

        if (storage instanceof IIngredientComponentStorageSlotted) {
            return ((IIngredientComponentStorageSlotted<T, M>) storage).getSlotContents(slotRelative);
        } else {
            try {
                return Iterators.get(storage.iterator(), slotRelative);
            } catch (IndexOutOfBoundsException e) {
                return getComponent().getMatcher().getEmptyInstance();
            }
        }
    }

    @Override
    public long getMaxQuantity(int slotAbsolute) {
        Pair<IIngredientComponentStorage<T, M>, Integer> storageAndSlot = getStorageAndRelativeSlot(slotAbsolute);
        IIngredientComponentStorage<T, M> storage = storageAndSlot.getLeft();
        int slotRelative = storageAndSlot.getRight();
        if (storage == null) {
            return 0;
        }

        if (storage instanceof IIngredientComponentStorageSlotted) {
            return ((IIngredientComponentStorageSlotted<T, M>) storage).getMaxQuantity(slotRelative);
        } else {
            return Helpers.castSafe(getComponent().getMatcher().getMaximumQuantity());
        }
    }

    @Override
    public T insert(int slotAbsolute, @Nonnull T ingredient, boolean simulate) {
        Pair<IIngredientComponentStorage<T, M>, Integer> storageAndSlot = getStorageAndRelativeSlot(slotAbsolute);
        IIngredientComponentStorage<T, M> storage = storageAndSlot.getLeft();
        int slotRelative = storageAndSlot.getRight();
        if (storage == null) {
            return ingredient;
        }

        if (storage instanceof IIngredientComponentStorageSlotted) {
            return ((IIngredientComponentStorageSlotted<T, M>) storage).insert(slotRelative, ingredient, simulate);
        } else {
            return storage.insert(ingredient, simulate);
        }
    }

    @Override
    public T extract(int slotAbsolute, long maxQuantity, boolean simulate) {
        Pair<IIngredientComponentStorage<T, M>, Integer> storageAndSlot = getStorageAndRelativeSlot(slotAbsolute);
        IIngredientComponentStorage<T, M> storage = storageAndSlot.getLeft();
        int slotRelative = storageAndSlot.getRight();
        if (storage == null) {
            return getComponent().getMatcher().getEmptyInstance();
        }

        if (storage instanceof IIngredientComponentStorageSlotted) {
            return ((IIngredientComponentStorageSlotted<T, M>) storage).extract(slotRelative, maxQuantity, simulate);
        } else {
            return storage.extract(maxQuantity, simulate);
        }
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
    public T insert(@Nonnull T ingredient, boolean simulate) {
        return channel.insert(ingredient, simulate);
    }

    @Override
    public T extract(@Nonnull T prototype, M matchCondition, boolean simulate) {
        return channel.extract(prototype, matchCondition, simulate);
    }

    @Override
    public T extract(long maxQuantity, boolean simulate) {
        return channel.extract(maxQuantity, simulate);
    }
}
