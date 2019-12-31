package org.cyclops.integrateddynamics.api.network;

import javax.annotation.Nullable;

/**
 * Capability for holding a network.
 * @author rubensworks
 */
public interface INetworkCarrier {

    /**
     * Tell the container it is part of the given network.
     * @param network The network.
     */
    public void setNetwork(@Nullable INetwork network);

    /**
     * Get the current container network. Can be null.
     * @return The network.
     */
    public INetwork getNetwork();

}
