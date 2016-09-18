package org.cyclops.integrateddynamics.capability.network;

import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkCarrier;

import javax.annotation.Nullable;

/**
 * Default implementation of {@link INetworkCarrier}.
 * @author rubensworks
 */
public class NetworkCarrierDefault implements INetworkCarrier {

    private INetwork network;

    @Override
    public void setNetwork(@Nullable INetwork network) {
        this.network = network;
    }

    @Nullable
    @Override
    public INetwork getNetwork() {
        return network;
    }
}
