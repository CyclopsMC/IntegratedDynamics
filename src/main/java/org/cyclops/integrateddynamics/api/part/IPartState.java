package org.cyclops.integrateddynamics.api.part;

import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

/**
 * A value holder for an {@link IPartType}.
 * This is what will be serialized from and to NBT.
 * This object is mutable and should not be recreated.
 * Note that you should be careful when passing around this state, because when the server sends an update to the
 * client, this state could be overwritten with a new version, so always try to use the part container to get the state.
 * @author rubensworks
 */
public interface IPartState<P extends IPartType> {

    public static final String GLOBALCOUNTER_KEY = "part";

    /**
     * Write a state to NBT.
     *
     * @param valueDeseralizationContext
     * @param tag                        The tag to write to.
     */
    public void writeToNBT(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag);

    /**
     * Read a state from NBT.
     * @param valueDeseralizationContext Getter for blocks.
     * @param tag The tag to read from.
     */
    public void readFromNBT(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag);

    /**
     * Generate a server-wide unique ID for this part state.
     */
    public void generateId();

    /**
     * A server-wide unique ID for this part that is persisted when the part is broken and moved.
     * @return The unique ID
     */
    public int getId();

    /**
     * Set the update interval for this state.
     * @param updateInterval The tick interval to update this element.
     */
    public void setUpdateInterval(int updateInterval);

    /**
     * @return The tick interval to update this element.
     */
    public int getUpdateInterval();

    /**
     * Set the priority of this part in the network.
     * @deprecated Should only be called from {@link org.cyclops.integrateddynamics.api.network.INetwork#setPriorityAndChannel(INetworkElement, int, int)}}!
     * @param priority The new priority
     */
    @Deprecated
    public void setPriority(int priority);

    /**
     * @return The priority of this part in the network.
     */
    public int getPriority();

    /**
     * Set the channel for this state.
     * @deprecated Should only be called from {@link org.cyclops.integrateddynamics.api.network.INetwork#setPriorityAndChannel(INetworkElement, int, int)}}!
     * @param channel The new channel
     */
    @Deprecated
    public void setChannel(int channel);

    /**
     * @return This part's channel.
     */
    public int getChannel();

    /**
     * @return The target position offset.
     */
    public Vec3i getTargetOffset();

    /**
     * @param offset The target position offset.
     */
    public void setTargetOffset(Vec3i offset);

    /**
     * Indicate that the given part should interact with the given side of the target.
     * @param side The side of the target block to interact with.
     *             Null removes the side override.
     */
    public void setTargetSideOverride(@Nullable Direction side);

    /**
     * @return The side of the target block to interact with. Can be null.
     */
    @Nullable
    public Direction getTargetSideOverride();

    /**
     * Indicate that this state has changes that must be saved to the world.
     */
    public void markDirty();

    /**
     * Check if dirty and reset the dirty state.
     * @return If this state has changed since the last time and needs to be persisted to NBT eventually.
     */
    public boolean isDirtyAndReset();

    /**
     * Check if this part state should update and reset the flag.
     * @return If this state has changed since the last time and needs to be updated to the client.
     */
    public boolean isUpdateAndReset();

    /**
     * Set a flag indicating that the next time that
     * {@link IPartType#shouldTriggerBlockRenderUpdate(IPartState, IPartState)}
     * is queried, it should return true.
     *
     * This is useful in cases where the player makes changes inside a part,
     * the state difference checking can not be relied upon,
     * and a state update should be forced in any case.
     *
     * This should only be called client-side.
     */
    public void forceBlockRenderUpdate();

    /**
     * @return If a block render update is forced.
     * This flagged will be set to false after this method is called.
     *
     * This should only be called client-side.
     */
    public boolean isForceBlockRenderUpdateAndReset();

    /**
     * Get the properties for the given aspect.
     * This will only retrieve the already saved properties, so this could be null if not set before.
     * It is better to call the {@link IAspect#getProperties(IPartType, PartTarget, IPartState)} method instead.
     * @param aspect The aspect to get the properties from.
     * @return The properties, this can be null if still the default.
     */
    public IAspectProperties getAspectProperties(IAspect aspect);

    /**
     * Set the properties for the given aspect.
     * @param aspect The aspect to get the properties from.
     * @param properties The properties, this can be null if still the default.
     */
    public void setAspectProperties(IAspect aspect, IAspectProperties properties);

    /**
     * Enable the part from working.
     * @param enabled If it should work.
     */
    public void setEnabled(boolean enabled);

    /**
     * @return If the part should work.
     */
    public boolean isEnabled();

    /**
     * Gathers the capabilities of this part state.
     * Don't call this unless you know what you're doing!
     * @param partType The part type this state is associated with.
     */
    public void gatherCapabilities(P partType);

    /**
     * Get the given capability.
     * @param <T> The capability type.
     * @param partType The part type.
     * @param capability The capability to get.
     * @param network The network the part belongs to.
     * @param partNetwork The part network the part belongs to.
     * @param target The target.
     * @return The optional capability instance.
     */
    public <T> Optional<T> getCapability(P partType, PartCapability<T> capability, INetwork network, IPartNetwork partNetwork, PartTarget target);

    /**
     * Add a capability to this state that will not be automatically persisted to NBT.
     * @param <T> The capability type.
     * @param capability The optional capability.
     * @param value The capability instance.
     */
    public <T> void addVolatileCapability(PartCapability<T> capability, Optional<T> value);

    /**
     * Remove a non-persisted capability.
     * @param capability The capability.
     */
    public void removeVolatileCapability(PartCapability<?> capability);

    /**
     * Load the inventory of the given name from the part state.
     * @param name The inventory name.
     * @param inventory The inventory object to load into.
     */
    public default void loadInventoryNamed(String name, Container inventory) {
        NonNullList<ItemStack> tabItems = this.getInventoryNamed(name);
        if (tabItems != null) {
            for (int i = 0; i < tabItems.size(); i++) {
                inventory.setItem(i, tabItems.get(i));
            }
        }
    }

    /**
     * Save the inventory of the given name into the part state.
     * @param name The inventory name.
     * @param inventory The inventory object to save.
     */
    public default void saveInventoryNamed(String name, Container inventory) {
        NonNullList<ItemStack> latestItems = NonNullList.create();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            latestItems.add(inventory.getItem(i));
        }
        this.setInventoryNamed(name, latestItems);
    }

    /**
     * @param name The inventory name.
     * @return Get the inventory contents of the given name.
     */
    @Nullable
    public NonNullList<ItemStack> getInventoryNamed(String name);

    /**
     * Set the inventory of the given name.
     * @param name The inventory name.
     * @param inventory Inventory contents.
     */
    public void setInventoryNamed(String name, NonNullList<ItemStack> inventory);

    /**
     * @return All named inventories.
     */
    public Map<String, NonNullList<ItemStack>> getInventoriesNamed();

    /**
     * Clear all named inventories.
     */
    public void clearInventoriesNamed();

    /**
     * Run the initialization logic for offset handling.
     */
    public void initializeOffsets(PartTarget target);

    /**
     * Tick any internal offset variables.
     * @param partType The part type.
     * @param network The network.
     * @param partNetwork The part network.
     * @param target The part target.
     */
    public void updateOffsetVariables(P partType, INetwork network, IPartNetwork partNetwork, PartTarget target);

    /**
     * Indicate that the contents of the offset variables inventory have changed.
     */
    public void markOffsetVariablesChanged();

    /**
     * @param slot The offset variable slot.
     * @return The current error, or null if no error.
     */
    @Nullable
    public MutableComponent getOffsetVariableError(int slot);

    /**
     * @return If the part contains variable-driven offsets that require updating.
     */
    public boolean requiresOffsetUpdates();

    /**
     * @return The max offset allowed in this part.
     */
    public int getMaxOffset();

    /**
     * Update the max offset for this part.
     * @param offset The new offset.
     */
    public void setMaxOffset(int offset);
}
