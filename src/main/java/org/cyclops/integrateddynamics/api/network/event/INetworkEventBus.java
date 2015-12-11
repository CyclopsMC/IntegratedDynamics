package org.cyclops.integrateddynamics.api.network.event;

import org.cyclops.integrateddynamics.api.network.IEventListenableNetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElement;

/**
 * An event bus for {@link org.cyclops.integrateddynamics.api.network.INetwork} events where
 * {@link INetworkElement} instances can listen to.
 * @author rubensworks
 */
public interface INetworkEventBus {

    /**
     * Register a network element for the given event type.
     * @param target The element that will be called once the event bus receives the given event.
     * @param eventType The event type.
     */
    public void register(IEventListenableNetworkElement<?> target, Class<? extends INetworkEvent> eventType);

    /**
     * Unregister a network element for the given event type.
     * @param target The element that would be called once the event bus receives the given event.
     * @param eventType The event type.
     */
    public void unregister(IEventListenableNetworkElement<?> target, Class<? extends INetworkEvent> eventType);

    /**
     * Unregister all events for the given network element.
     * @param target The element that would be called once the event bus receives events.
     */
    public void unregister(IEventListenableNetworkElement<?> target);

    /**
     * Post the given event to the events bus.
     * @param event The event to post.
     */
    public void post(INetworkEvent event);

    /**
     * Post the given cancelable event to the events bus.
     * @param event The event to post.
     * @return If the event was not canceled.
     */
    public boolean postCancelable(ICancelableNetworkEvent event);

}
