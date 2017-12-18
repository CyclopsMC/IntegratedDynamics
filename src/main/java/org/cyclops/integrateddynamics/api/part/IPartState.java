package org.cyclops.integrateddynamics.api.part;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;

import javax.annotation.Nullable;

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
     * @param tag The tag to write to.
     */
    public void writeToNBT(NBTTagCompound tag);

    /**
     * Read a state from NBT.
     * @param tag The tag to read from.
     */
    public void readFromNBT(NBTTagCompound tag);

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
     * Indicate that the given part should interact with the given side of the target.
     * @param side The side of the target block to interact with.
     *             Null removes the side override.
     */
    public void setTargetSideOverride(@Nullable EnumFacing side);

    /**
     * @return The side of the target block to interact with. Can be null.
     */
    @Nullable
    public EnumFacing getTargetSideOverride();

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
     * If this part state has the given capability.
     * @param capability The capability to check.
     * @return If this has the given capability/
     */
    boolean hasCapability(Capability<?> capability);

    /**
     * Get the given capability.
     * @param capability The capability to get.
     * @param <T> The capability type.
     * @return The capability instance.
     */
    <T> T getCapability(Capability<T> capability);

    /**
     * Add a capability to this state that will not be automatically persisted to NBT.
     * @param capability The capability.
     * @param value The capability instance.
     * @param <T> The capability type.
     */
    public <T> void addVolatileCapability(Capability<T> capability, T value);

    /**
     * Remove a non-persisted capability.
     * @param capability The capability.
     */
    public void removeVolatileCapability(Capability<?> capability);

}
