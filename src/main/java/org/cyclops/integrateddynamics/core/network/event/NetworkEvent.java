package org.cyclops.integrateddynamics.core.network.event;

import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;

/**
 * An event posted in the {@link INetwork} event bus.
 * @author rubensworks
 */
public class NetworkEvent implements INetworkEvent {

    private final INetwork network;

    public NetworkEvent(INetwork network) {
        this.network = network;
    }

    @Override
    public INetwork getNetwork() {
        return this.network;
    }

}
