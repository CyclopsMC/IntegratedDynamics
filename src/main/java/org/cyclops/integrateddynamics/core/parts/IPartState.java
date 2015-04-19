package org.cyclops.integrateddynamics.core.parts;

import net.minecraft.nbt.NBTTagCompound;

/**
 * A value holder for an {@link org.cyclops.integrateddynamics.core.parts.IPart}.
 * This is what will be serialized from and to NBT.
 * @author rubensworks
 */
public interface IPartState<P extends IPart> {

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
