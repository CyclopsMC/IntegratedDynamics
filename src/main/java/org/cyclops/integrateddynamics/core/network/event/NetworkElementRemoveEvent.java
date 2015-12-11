package org.cyclops.integrateddynamics.core.network.event;

import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.event.ICancelableNetworkEvent;

/**
 * An event thrown when an {@link INetworkElement} is being removed from the network.
 * @author rubensworks
 */
public abstract class NetworkElementRemoveEvent extends NetworkEvent {

    private final INetworkElement networkElement;

    protected NetworkElementRemoveEvent(INetwork network, INetworkElement networkElement) {
        super(network);
        this.networkElement = networkElement;
    }

    public INetworkElement getNetworkElement() {
        return this.networkElement;
    }

    /**
     * A cancelable event before the element is removed from the network.
     * Canceling this event will prevent the network element from being removed.
     */
    public static class Pre extends NetworkElementRemoveEvent implements ICancelableNetworkEvent {

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
     * After the element has been removed from the network.
     */
    public static class Post extends NetworkElementRemoveEvent {

        public Post(INetwork network, INetworkElement networkElement) {
            super(network, networkElement);
        }

    }

}
