package org.cyclops.integrateddynamics.capability.network;

import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkCarrier;

import javax.annotation.Nullable;

/**
 * Default implementation of {@link INetworkCarrier}.
 * @author rubensworks
 */
public class NetworkCarrierDefault<N extends INetwork> implements INetworkCarrier<N> {

    private N network;

    @Override
    public void setNetwork(@Nullable N network) {
        this.network = network;
    }

    @Nullable
    @Override
    public N getNetwork() {
        return network;
    }
}
