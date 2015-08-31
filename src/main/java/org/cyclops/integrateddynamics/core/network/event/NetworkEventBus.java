package org.cyclops.integrateddynamics.core.network.event;

import com.google.common.collect.Maps;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.cyclops.cyclopscore.helper.CollectionHelpers;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * An event bus for {@link org.cyclops.integrateddynamics.core.network.Network} events where
 * {@link org.cyclops.integrateddynamics.core.network.INetworkElement} instances can listen to.
 *
 * Partially based on Minecraft Forge's {@link EventBus} implementation.
 *
 * @author rubensworks
 */
public class NetworkEventBus {

    private final Map<Class<? extends NetworkEvent>, Set<IEventListenableNetworkElement<?>>> listeners = Collections.synchronizedMap(Maps.<Class<? extends NetworkEvent>, Set<IEventListenableNetworkElement<?>>>newHashMap());

    /**
     * Register a network element for the given event type.
     * @param target The element that will be called once the event bus receives the given event.
     * @param eventType The event type.
     */
    public void register(IEventListenableNetworkElement<?> target, Class<? extends NetworkEvent> eventType) {
        CollectionHelpers.addToMapSet(this.listeners, eventType, target);
    }

    /**
     * Unregister a network element for the given event type.
     * @param target The element that would be called once the event bus receives the given event.
     * @param eventType The event type.
     */
    public void unregister(IEventListenableNetworkElement<?> target, Class<? extends NetworkEvent> eventType) {
        Set<IEventListenableNetworkElement<?>> listeners = this.listeners.get(eventType);
        listeners.remove(target);
    }

    /**
     * Unregister all events for the given network element.
     * @param target The element that would be called once the event bus receives events.
     */
    public void unregister(IEventListenableNetworkElement<?> target) {
        for(Class<? extends NetworkEvent> eventType : target.getNetworkEventListener().getSubscribedEvents()) {
            unregister(target, eventType);
        }
    }

    /**
     * Post the given event to the events bus.
     * @param event The event to post.
     */
    public void post(NetworkEvent event) {
        Set<IEventListenableNetworkElement<?>> listeners = this.listeners.getOrDefault(event.getClass(), Collections.<IEventListenableNetworkElement<?>>emptySet());
        for (IEventListenableNetworkElement listener : listeners) {
            listener.getNetworkEventListener().onEvent(event, listener);
        }
    }

}
