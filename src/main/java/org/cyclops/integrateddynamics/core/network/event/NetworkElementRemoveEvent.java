package org.cyclops.integrateddynamics.core.network.event;

import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.event.ICancelableNetworkEvent;

/**
 * An event thrown when an {@link INetworkElement} is being removed from the network.
 * @author rubensworks
 */
public abstract class NetworkElementRemoveEvent<N extends INetwork<N>> extends NetworkEvent<N> {

    private final INetworkElement<N> networkElement;

    protected NetworkElementRemoveEvent(N network, INetworkElement<N> networkElement) {
        super(network);
        this.networkElement = networkElement;
    }

    public INetworkElement<N> getNetworkElement() {
        return this.networkElement;
    }

    /**
     * A cancelable event before the element is removed from the network.
     * Canceling this event will prevent the network element from being removed.
     */
    public static class Pre<N extends INetwork<N>> extends NetworkElementRemoveEvent<N> implements ICancelableNetworkEvent<N> {

        private boolean canceled = false;

        public Pre(N network, INetworkElement<N> networkElement) {
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
    public static class Post<N extends INetwork<N>> extends NetworkElementRemoveEvent<N> {

        public Post(N network, INetworkElement<N> networkElement) {
            super(network, networkElement);
        }

    }

}
