package org.cyclops.integrateddynamics.core.part;

import net.minecraft.nbt.NBTTagCompound;

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
     * Get the part state class.
     * This is used for doing dynamic construction of guis.
     * @return The actual class for this part state.
     */
    public Class<? extends IPartState> getPartStateClass();

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
     * Check if dirty and reset the dirty state.
     * @return If this state has changed since the last time and needs to be persisted to NBT eventually.
     */
    public boolean isDirtyAndReset();

    /**
     * Check if this part state should update and reset the flag.
     * @return If this state has changed since the last time and needs to be updated to the client.
     */
    public boolean isUpdateAndReset();

}
