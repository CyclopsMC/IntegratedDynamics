package org.cyclops.integrateddynamics.core.part;

import net.minecraft.nbt.NBTTagCompound;

/**
 * A type of part that can be inserted into a
 * {@link org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking}.
 * Only one unique instance for each part should exist, the values are stored inside an
 * {@link org.cyclops.integrateddynamics.core.part.IPartState}.
 * @author rubensworks
 */
public interface IPartType<P extends IPartType<P, S>, S extends IPartState<P>> {

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

    /**
     * @param state The state
     * @return The tick interval to update this element.
     */
    public int getUpdateInterval(IPartState<P> state);

    /**
     * @param state The state
     * @return If this element should be updated. This method is only called once during network initialization.
     */
    public boolean isUpdate(IPartState<P> state);

    /**
     * @param state The state
     * Update at the tick interval specified.
     */
    public void update(IPartState<P> state);

    /**
     * @param state The state
     * Called right before the network is terminated or will be reset.
     */
    public void beforeNetworkKill(IPartState<P> state);

    /**
     * @param state The state
     * Called right after this network is initialized.
     */
    public void afterNetworkAlive(IPartState<P> state);

}
