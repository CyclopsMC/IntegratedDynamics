package org.cyclops.integrateddynamics.api.network;

import java.util.Optional;

/**
 * Network elements that can delegate to elements that are automatically subscribed to the
 * {@link org.cyclops.integrateddynamics.api.network.event.INetworkEventBus}.
 * @param <D> The type of delegate that will be used as event listener for this network. Could be self or null.
 * @author rubensworks
 */
public interface IEventListenableNetworkElement<D extends INetworkEventListener<?>> extends INetworkElement {

    /**
     * This listener will never be saved as an instance, this network element is always used as delegator to this listener.
     * @return The optional event listener.
     */
    public Optional<D> getNetworkEventListener();

}
