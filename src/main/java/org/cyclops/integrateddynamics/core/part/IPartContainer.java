package org.cyclops.integrateddynamics.core.part;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import org.cyclops.cyclopscore.datastructure.DimPos;

import java.util.Map;

/**
 * An interface for containers that can hold {@link IPartType}s.
 * @author rubensworks
 */
public interface IPartContainer {

    /**
     * @return The position this container is at.
     */
    public DimPos getPosition();

    /**
     * @return The parts inside this container.
     */
    public Map<EnumFacing, IPartType<?, ?>> getParts();

    /**
     * @return If this container has at least one part.
     */
    public boolean hasParts();

    /**
     * Set the part for a side.
     * @param side The side to place the part on.
     * @param part The part.
     * @param partState The state for this part.
     * @param <P> The type of part.
     * @param <S> The type of part state.
     */
    public <P extends IPartType<P, S>, S extends IPartState<P>> void setPart(EnumFacing side, IPartType<P, S> part,
                                                                             IPartState<P> partState);

    /**
     * Get the part of a side, can be null.
     * @param side The side.
     * @return The part or null.
     */
    public IPartType getPart(EnumFacing side);

    /**
     * @param side The side.
     * @return If the given side has a part.
     */
    public boolean hasPart(EnumFacing side);

    /**
     * Remove the part from a side, can return null if there was no part on that side.
     * @param side The side.
     * @param player The player removing the part.
     * @return The removed part or null.
     */
    public IPartType removePart(EnumFacing side, EntityPlayer player);

    /**
     * Set the state of a part.
     * @param side The side.
     * @param partState The part state.
     */
    public void setPartState(EnumFacing side, IPartState partState);

    /**
     * Get the state of a part.
     * @param side The side.
     * @return The part state.
     */
    public IPartState getPartState(EnumFacing side);

}
