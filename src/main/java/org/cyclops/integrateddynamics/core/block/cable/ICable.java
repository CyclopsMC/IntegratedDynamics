package org.cyclops.integrateddynamics.core.block.cable;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.core.path.IPathElement;
import org.cyclops.integrateddynamics.core.path.IPathElementProvider;

/**
 * Interface for blocks that can connect with cables.
 * @author rubensworks
 */
public interface ICable<E extends IPathElement<E>> extends IPathElementProvider<E> {

    /**
     * Check if the given position should connect with this.
     * @param world The world.
     * @param selfPosition The position for this block.
     * @param connector The connecting block.
     * @param side The side of the connecting block.
     * @return If it should connect.
     */
    public boolean canConnect(World world, BlockPos selfPosition, ICable connector, EnumFacing side);

    /**
     * Update the cable connections at the given position.
     * @param world The world.
     * @param pos The position of this block.
     */
    public void updateConnections(World world, BlockPos pos);

    /**
     * Check if this cable is connected to a side.
     * @param world The world.
     * @param pos The position of this block.
     * @param side The side to check a connection for.
     * @return If this block is connected with that side.
     */
    public boolean isConnected(World world, BlockPos pos, EnumFacing side);

    /**
     * Disconnect the cable connection for a side.
     * @param world The world.
     * @param pos The position of this block.
     * @param side The side to block the connection for.
     */
    public void disconnect(World world, BlockPos pos, EnumFacing side);

    /**
     * Reconnect the cable connection for a side.
     * Will only do something if the cable was previously disconnected.
     * @param world The world.
     * @param pos The position of this block.
     * @param side The side to remake the connection for.
     */
    public void reconnect(World world, BlockPos pos, EnumFacing side);

}
