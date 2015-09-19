package org.cyclops.integrateddynamics.core.tileentity;

import net.minecraft.util.EnumFacing;
import org.cyclops.integrateddynamics.core.block.cable.ICable;

/**
 * Interface for tile entities behind block that are a {@link org.cyclops.integrateddynamics.core.block.cable.ICable}.
 * @author rubensworks
 */
public interface ITileCable {

    /**
     * Check if this tile should connect with this.
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
