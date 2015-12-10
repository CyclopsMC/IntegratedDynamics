package org.cyclops.integrateddynamics.api.network.event;

import org.cyclops.integrateddynamics.api.network.INetwork;

/**
 * An event posted in the {@link INetwork} event bus.
 * @author rubensworks
 */
public interface INetworkEvent {

    /**
     * @return The network this event is thrown in.
     */
    public INetwork getNetwork();

}
