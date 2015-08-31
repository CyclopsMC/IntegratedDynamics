package org.cyclops.integrateddynamics.core.network.event;

import org.cyclops.integrateddynamics.core.network.Network;

/**
 * An event posted in the {@link org.cyclops.integrateddynamics.core.network.Network} event bus.
 * @author rubensworks
 */
public class NetworkEvent {

    private final Network network;

    public NetworkEvent(Network network) {
        this.network = network;
    }

    public Network getNetwork() {
        return this.network;
    }

}
