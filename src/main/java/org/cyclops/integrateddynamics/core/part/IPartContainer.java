package org.cyclops.integrateddynamics.core.part;

import net.minecraft.util.EnumFacing;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.core.network.Network;

import java.util.Map;

/**
 * A interface for containers that can hold {@link IPartType}s.
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
     * Set the part for a side.
     * @param side The side to place the part on.
     * @param part The part.
     * @param <P> The type of part.
     * @param <S> The type of part state.
     */
    public <P extends IPartType<P, S>, S extends IPartState<P>> void setPart(EnumFacing side, IPartType<P, S> part);

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
     * @return The removed part or null.
     */
    public IPartType removePart(EnumFacing side);

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

    /**
     * Tell the container it is no longer part of its current network.
     * Won't do anything if it doesn't have a network.
     */
    public void resetCurrentNetwork();

    /**
     * Tell the container it is part of the given network.
     * @param network The network.
     */
    public void setNetwork(Network network);

    /**
     * Get the current container network. Can be null.
     */
    public Network getNetwork();

}
