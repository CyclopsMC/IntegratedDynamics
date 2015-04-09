package org.cyclops.integrateddynamics.core.parts;

import net.minecraft.nbt.NBTTagCompound;

/**
 * A type of part that can be inserted into a
 * {@link org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking}.
 * Only one unique instance for each part should exist, the values are stored inside an
 * {@link org.cyclops.integrateddynamics.core.parts.IPartState}.
 * @author rubensworks
 */
public interface IPart<P extends IPart<P, S>, S extends IPartState<P>> {

    /**
     * @return The part type.
     */
    public EnumPartType getType();

    /**
     * Write the properties of this part to NBT.
     * An identificator for this part is not required, this is written somewhere else.
     * @param tag The tag to write to. This tag is guaranteed to be empty.
     * @param partState The state of this part.
     */
    public void toNBT(NBTTagCompound tag, S partState);

    /**
     * Read the properties of this part from nbt.
     * This tag is guaranteed to only contain data for this part.
     * @param tag The tag to read from.
     * @return The state of this part.
     */
    public S fromNBT(NBTTagCompound tag);

    /**
     * @return The default state of this part.
     */
    public IPartState<P> getDefaultState();

}
