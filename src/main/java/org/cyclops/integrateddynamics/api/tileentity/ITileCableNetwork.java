package org.cyclops.integrateddynamics.api.tileentity;

import org.cyclops.integrateddynamics.api.block.cable.ICableNetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;

/**
 * Interface for tile entities behind block that are a {@link ICableNetwork}.
 * @author rubensworks
 */
public interface ITileCableNetwork extends ITileCable {

    /**
     * Tell the container it is no longer part of its current network.
     * Won't do anything if it doesn't have a network.
     */
    public void resetCurrentNetwork();

    /**
     * Tell the container it is part of the given network.
     * @param network The network.
     */
    public void setNetwork(IPartNetwork network);

    /**
     * Get the current container network. Can be null.
     * @return The network.
     */
    public IPartNetwork getNetwork();

}
