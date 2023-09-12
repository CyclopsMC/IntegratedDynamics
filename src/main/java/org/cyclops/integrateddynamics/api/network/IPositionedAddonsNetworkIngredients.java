package org.cyclops.integrateddynamics.api.network;

import com.google.common.collect.Iterators;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorageSlotted;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorageWrapperHandler;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentStorageObservable;
import org.cyclops.integrateddynamics.api.part.PartPos;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * An ingredient network that can hold prioritized positions.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter, may be Void. Instances MUST properly implement the equals method.
 * @author rubensworks
 */
public interface IPositionedAddonsNetworkIngredients<T, M> extends IPositionedAddonsNetwork,
        IIngredientComponentStorageObservable<T, M> {

    /**
     * @return The ingredient component type this storage applies to.
     */
    public IngredientComponent<T, M> getComponent();

    /**
     * @return The quantity rate limit for each storage mutation.
     */
    public long getRateLimit();

    /**
     * Get the storage at the given position.
     * @param pos A position.
     * @return The storage, or an empty storage if none is available.
     */
    public IIngredientComponentStorage<T, M> getPositionedStorage(PartPos pos);

    /**
     * Set an ingredient filter for the given storage position.
     * Unsets the filter if null is provided.
     * @param pos A position.
     * @param filter An ingredient filter.
     */
    public void setPositionedStorageFilter(PartPos pos, @Nullable PositionedAddonsNetworkIngredientsFilter<T> filter);

    /**
     * @param pos A position.
     * @return An optional ingredient filter for the given storage position.
     */
    @Nullable
    public PositionedAddonsNetworkIngredientsFilter<T> getPositionedStorageFilter(PartPos pos);

    /**
     * Get all instances at the target position.
     * @param pos A part position.
     * @return A collection of instances. This can not be a view, and must be a deep copy of the target.
     */
    public default Iterator<T> getRawInstances(PartPos pos) {
        Iterator<T> it = getPositionedStorage(pos).iterator();
        PositionedAddonsNetworkIngredientsFilter<T> filter = getPositionedStorageFilter(pos);
        if (filter != null) {
            it = Iterators.filter(it, filter::testView);
        }
        return it;
    }

    /**
     * Get the storage at the given position.
     * @param pos A position.
     * @return The storage.
     */
    @Nullable
    public default IIngredientComponentStorage<T, M> getPositionedStorageUnsafe(PartPos pos) {
        DimPos dimPos = pos.getPos();
        Level world = dimPos.getLevel(true);
        if (world == null) {
            return null;
        }
        Optional<BlockEntity> tile = BlockEntityHelpers.get(world, dimPos.getBlockPos(), BlockEntity.class);
        return tile
                .map(tileEntity -> getComponent().getStorage(tileEntity, pos.getSide()))
                .orElse(null);
    }

    /**
     * Get the storage at the given channel.
     * @param channel A channel id.
     * @return A storage.
     */
    public INetworkIngredientsChannel<T, M> getChannelInternal(int channel);

    /**
     * Get the storage at the given channel.
     * @param channel A channel id.
     * @return A storage.
     * @deprecated
     */
    @Deprecated
    public default IIngredientComponentStorage<T, M> getChannel(int channel) { // TODO: remove in next major update, and rename method above to getChannel
        return this.getChannelInternal(channel);
    }

    /**
     * Get the slotted storage at the given channel.
     * @param channel A channel id.
     * @return A slotted storage.
     */
    public IIngredientComponentStorageSlotted<T, M> getChannelSlotted(int channel);

    /**
     * Get the external storage at the given channel.
     * @param capability A capability to wrap the channel in.
     * @param channel A channel id.
     * @param <S> The external storage type.
     * @return An external storage, or null if no wrapping is possible for the given capability.
     */
    @Nullable
    public default <S> S getChannelExternal(Capability<S> capability, int channel) {
        IIngredientComponentStorageWrapperHandler<T, M, S> wrapperHandler = getComponent()
                .getStorageWrapperHandler(capability);
        return wrapperHandler != null ? wrapperHandler.wrapStorage(getChannel(channel)) : null;
    }

    /**
     * Get the last tick duration of the index observer.
     * @return Duration in nanoseconds
     */
    public Map<PartPos, Long> getLastSecondDurationIndex();

    /**
     * Reset the last second duration count.
     */
    public void resetLastSecondDurationsIndex();

}
