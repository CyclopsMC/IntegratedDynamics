package org.cyclops.integrateddynamics.core.network.event;

import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;

/**
 * An event posted in the {@link IPartNetwork} event bus.
 * @author rubensworks
 */
public class NetworkEvent<N extends INetwork<N>> implements INetworkEvent<N> {

    private final N network;

    public NetworkEvent(N network) {
        this.network = network;
    }

    @Override
    public N getNetwork() {
        return this.network;
    }

}
