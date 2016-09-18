package org.cyclops.integrateddynamics.core.network.event;

import com.google.common.collect.Maps;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.cyclops.cyclopscore.helper.CollectionHelpers;
import org.cyclops.integrateddynamics.api.network.IEventListenableNetworkElement;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.event.ICancelableNetworkEvent;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.api.network.event.INetworkEventBus;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * An event bus for {@link INetwork} events where
 * {@link INetworkElement} instances can listen to.
 *
 * Partially based on Minecraft Forge's {@link EventBus} implementation.
 *
 * @author rubensworks
 */
public class NetworkEventBus implements INetworkEventBus {

    private final Map<Class<? extends INetworkEvent>, Set<IEventListenableNetworkElement<?>>> listeners = Collections.synchronizedMap(Maps.<Class<? extends INetworkEvent>, Set<IEventListenableNetworkElement<?>>>newHashMap());

    @Override
    public void register(IEventListenableNetworkElement<?> target, Class<? extends INetworkEvent> eventType) {
        CollectionHelpers.addToMapSet(this.listeners, eventType, target);
    }

    @Override
    public void unregister(IEventListenableNetworkElement<?> target, Class<? extends INetworkEvent> eventType) {
        Set<IEventListenableNetworkElement<?>> listeners = this.listeners.get(eventType);
        if(listeners != null) {
            listeners.remove(target);
        }
    }

    @Override
    public void unregister(IEventListenableNetworkElement<?> target) {
        for(Class<? extends INetworkEvent> eventType : target.getNetworkEventListener().getSubscribedEvents()) {
            unregister(target, eventType);
        }
    }

    @Override
    public void post(INetworkEvent event) {
        Set<IEventListenableNetworkElement<?>> listeners = this.listeners.get(event.getClass());
        if(listeners != null) {
            for (IEventListenableNetworkElement listener : listeners) {
                listener.getNetworkEventListener().onEvent(event, listener);
            }
        }
    }

    @Override
    public boolean postCancelable(ICancelableNetworkEvent event) {
        post(event);
        return !event.isCanceled();
    }

}
