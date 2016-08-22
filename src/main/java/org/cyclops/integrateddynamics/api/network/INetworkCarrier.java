package org.cyclops.integrateddynamics.api.network;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Interface for blocks that are network-aware.
 * @author rubensworks
 */
public interface INetworkCarrier<N extends INetwork> {

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
    public void setNetwork(N network, World world, BlockPos pos);

    /**
     * Get the current container network. Can be null.
     * @param world The world.
     * @param pos The position.
     * @return The network.
     */
    public @Nullable N getNetwork(World world, BlockPos pos);

}
