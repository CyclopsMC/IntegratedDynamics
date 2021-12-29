package org.cyclops.integrateddynamics.api.block.cable;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

/**
 * Capability for cables that can form networks.
 * Note that this is an UNSIDED capability, so only one should exist per side.
 * This is because cable sides are too dependent of each other.
 * TODO: In the future, this should become a sided capability.
 * @author rubensworks
 */
public interface ICable {

    /**
     * Check if this part should connect with the given cable for the given side.
     * This method MUST NOT call the {@link ICable#canConnect(ICable, Direction)} method
     * of the connector, this is checked externally, otherwise infinite loops will occur.
     * @param connector The connecting block.
     * @param side The side of the connecting block.
     * @return If it should connect.
     */
    public boolean canConnect(ICable connector, Direction side);

    /**
     * Update the cable connections.
     */
    public void updateConnections();

    /**
     * Check if this cable is connected to a side.
     * This method should not check any neighbours,
     * it should internally store the connection.
     * @param side The side to check a connection for.
     * @return If this block is connected with that side.
     */
    public boolean isConnected(Direction side);

    /**
     * Disconnect the cable connection for a side.
     * @param side The side to block the connection for.
     */
    public void disconnect(Direction side);

    /**
     * Reconnect the cable connection for a side.
     * Will only do something if the cable was previously disconnected.
     * @param side The side to remake the connection for.
     */
    public void reconnect(Direction side);

    /**
     * @return The item stack that is dropped when breaking this cable, when using a wrench for example.
     */
    public ItemStack getItemStack();

    /**
     * Called when this cable is removed by a wrench.
     */
    public void destroy();
}
