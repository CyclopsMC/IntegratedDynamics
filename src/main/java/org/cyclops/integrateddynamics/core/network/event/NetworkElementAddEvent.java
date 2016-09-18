package org.cyclops.integrateddynamics.core.network.event;

import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.event.ICancelableNetworkEvent;

/**
 * An event thrown when an {@link INetworkElement} is being added to the network.
 * @author rubensworks
 */
public abstract class NetworkElementAddEvent extends NetworkEvent {

    private final INetworkElement networkElement;

    protected NetworkElementAddEvent(INetwork network, INetworkElement networkElement) {
        super(network);
        this.networkElement = networkElement;
    }

    public INetworkElement getNetworkElement() {
        return this.networkElement;
    }

    /**
     * A cancelable event before the element is added to the network.
     * Canceling this event will prevent the network element from being added.
     */
    public static class Pre extends NetworkElementAddEvent implements ICancelableNetworkEvent {

        private boolean canceled = false;

        public Pre(INetwork network, INetworkElement networkElement) {
            super(network, networkElement);
        }

        @Override
        public void cancel() {
            this.canceled = true;
        }

        @Override
        public boolean isCanceled() {
            return this.canceled;
        }

    }

    /**
     * After the element has been added to the network.
     */
    public static class Post extends NetworkElementAddEvent {

        public Post(INetwork network, INetworkElement networkElement) {
            super(network, networkElement);
        }

    }

}
