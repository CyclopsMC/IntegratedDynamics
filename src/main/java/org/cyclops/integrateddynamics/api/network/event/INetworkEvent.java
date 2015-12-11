package org.cyclops.integrateddynamics.api.network.event;

import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;

/**
 * An event posted in the {@link IPartNetwork} event bus.
 * @author rubensworks
 */
public interface INetworkEvent<N extends INetwork<N>> {

    /**
     * @return The network this event is thrown in.
     */
    public N getNetwork();

}
