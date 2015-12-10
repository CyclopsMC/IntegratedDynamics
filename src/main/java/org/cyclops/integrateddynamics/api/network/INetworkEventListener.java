package org.cyclops.integrateddynamics.api.network;

import org.cyclops.integrateddynamics.core.network.event.NetworkEvent;

import java.util.Set;

/**
 * Interface to indicate delegates of a network element instance.
 * @author rubensworks
 */
public interface INetworkEventListener<E> {

    /**
     * @return If this should be registered to the network event bus for listening to network events.
     */
    public boolean hasEventSubscriptions();

    /**
     * @return The static set of events this listener should be subscribed to.
     */
    public Set<Class<? extends NetworkEvent>> getSubscribedEvents();

    /**
     * Can be called at any time by the {@link org.cyclops.integrateddynamics.core.network.event.NetworkEventBus}.
     * Only events in the set from {@link INetworkEventListener#getSubscribedEvents()} will be received.
     * @param event The received event.
     * @param networkElement The network element.
     */
    public void onEvent(NetworkEvent event, E networkElement);

}
