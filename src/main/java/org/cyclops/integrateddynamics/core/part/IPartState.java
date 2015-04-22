package org.cyclops.integrateddynamics.core.part;

import net.minecraft.nbt.NBTTagCompound;

/**
 * A value holder for an {@link IPartType}.
 * This is what will be serialized from and to NBT.
 * @author rubensworks
 */
public interface IPartState<P extends IPartType> {

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

}
