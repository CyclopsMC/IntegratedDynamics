package org.cyclops.integrateddynamics.core.network.event;

import com.google.common.collect.Maps;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.cyclops.cyclopscore.helper.CollectionHelpers;
import org.cyclops.integrateddynamics.api.network.IEventListenableNetworkElement;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.event.ICancelableNetworkEvent;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.api.network.event.INetworkEventBus;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * An event bus for {@link IPartNetwork} events where
 * {@link INetworkElement} instances can listen to.
 *
 * Partially based on Minecraft Forge's {@link EventBus} implementation.
 *
 * @author rubensworks
 */
public class NetworkEventBus<N extends INetwork<N>> implements INetworkEventBus<N> {

    private final Map<Class<? extends INetworkEvent<N>>, Set<IEventListenableNetworkElement<N, ?>>> listeners = Collections.synchronizedMap(Maps.<Class<? extends INetworkEvent<N>>, Set<IEventListenableNetworkElement<N, ?>>>newHashMap());

    @Override
    public void register(IEventListenableNetworkElement<N, ?> target, Class<? extends INetworkEvent<N>> eventType) {
        CollectionHelpers.addToMapSet(this.listeners, eventType, target);
    }

    @Override
    public void unregister(IEventListenableNetworkElement<N, ?> target, Class<? extends INetworkEvent<N>> eventType) {
        Set<IEventListenableNetworkElement<N, ?>> listeners = this.listeners.get(eventType);
        listeners.remove(target);
    }

    @Override
    public void unregister(IEventListenableNetworkElement<N, ?> target) {
        for(Class<? extends INetworkEvent<N>> eventType : target.getNetworkEventListener().getSubscribedEvents()) {
            unregister(target, eventType);
        }
    }

    @Override
    public void post(INetworkEvent<N> event) {
        Set<IEventListenableNetworkElement<N, ?>> listeners = this.listeners.get(event.getClass());
        if(listeners != null) {
            for (IEventListenableNetworkElement listener : listeners) {
                listener.getNetworkEventListener().onEvent(event, listener);
            }
        }
    }

    @Override
    public boolean postCancelable(ICancelableNetworkEvent<N> event) {
        post(event);
        return !event.isCanceled();
    }

}
