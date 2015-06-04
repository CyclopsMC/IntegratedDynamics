package org.cyclops.integrateddynamics.core.network;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * Interface for blocks that are network-aware.
 * @author rubensworks
 */
public interface INetworkCarrier {

    /**
     * Tell the container it is no longer part of its current network.
     * Won't do anything if it doesn't have a network.
     * @param world The world.
     * @param pos The position.
     */
    public void resetCurrentNetwork(World world, BlockPos pos);

    /**
     * Tell the container it is part of the given network.
     * @param network The network.
     * @param world The world.
     * @param pos The position.
     */
    public void setNetwork(Network network, World world, BlockPos pos);

    /**
     * Get the current container network. Can be null.
     */
    public Network getNetwork(World world, BlockPos pos);

}
