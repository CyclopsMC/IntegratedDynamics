package org.cyclops.integrateddynamics.api.block.cable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.api.path.IPathElementProvider;

import javax.annotation.Nullable;

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
     * This will not trigger neighbour cable connection updates.
     * @param world The world.
     * @param pos The position of this block.
     */
    public void updateConnections(World world, BlockPos pos);

    /**
     * Trigger a call of {@link ICable#updateConnections(World, BlockPos)}
     * for all connected neighbours
     * @param world The world.
     * @param pos The position of this block.
     */
    public void triggerUpdateNeighbourConnections(World world, BlockPos pos);

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

    /**
     * Remove this cable from the world and drop it.
     * @param world The world.
     * @param pos The position.
     * @param player The player removing the cable.
     */
    public void remove(World world, BlockPos pos, @Nullable EntityPlayer player);

}
