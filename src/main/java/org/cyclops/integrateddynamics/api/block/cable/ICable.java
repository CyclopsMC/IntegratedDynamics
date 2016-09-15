package org.cyclops.integrateddynamics.api.block.cable;

import net.minecraft.util.EnumFacing;

/**
 * Capability for cables that can form networks.
 * @author rubensworks
 */
public interface ICable { // TODO: see if we can modify this to be sided

    /**
     * Check if this part should connect with the given cable for the given side.
     * This method MUST NOT call the {@link ICable#canConnect(ICable, EnumFacing)} method
     * of the connector, this is checked externally, otherwise infinite loops will occur.
     * @param connector The connecting block.
     * @param side The side of the connecting block.
     * @return If it should connect.
     */
    public boolean canConnect(ICable connector, EnumFacing side);

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
    public boolean isConnected(EnumFacing side);

    /**
     * Disconnect the cable connection for a side.
     * @param side The side to block the connection for.
     */
    public void disconnect(EnumFacing side);

    /**
     * Reconnect the cable connection for a side.
     * Will only do something if the cable was previously disconnected.
     * @param side The side to remake the connection for.
     */
    public void reconnect(EnumFacing side);

}
