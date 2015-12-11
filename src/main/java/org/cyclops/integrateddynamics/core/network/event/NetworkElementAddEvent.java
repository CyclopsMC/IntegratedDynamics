package org.cyclops.integrateddynamics.core.network.event;

import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.event.ICancelableNetworkEvent;

/**
 * An event thrown when an {@link INetworkElement} is being added to the network.
 * @author rubensworks
 */
public abstract class NetworkElementAddEvent<N extends INetwork<N>> extends NetworkEvent<N> {

    private final INetworkElement<N> networkElement;

    protected NetworkElementAddEvent(N network, INetworkElement<N> networkElement) {
        super(network);
        this.networkElement = networkElement;
    }

    public INetworkElement<N> getNetworkElement() {
        return this.networkElement;
    }

    /**
     * A cancelable event before the element is added to the network.
     * Canceling this event will prevent the network element from being added.
     */
    public static class Pre<N extends INetwork<N>> extends NetworkElementAddEvent<N> implements ICancelableNetworkEvent<N> {

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
     * After the element has been added to the network.
     */
    public static class Post<N extends INetwork<N>> extends NetworkElementAddEvent<N> {

        public Post(N network, INetworkElement<N> networkElement) {
            super(network, networkElement);
        }

    }

}
