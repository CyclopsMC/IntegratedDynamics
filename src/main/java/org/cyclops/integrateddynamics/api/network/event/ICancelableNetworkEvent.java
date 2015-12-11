package org.cyclops.integrateddynamics.api.network.event;

import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;

/**
 * An event posted in the {@link IPartNetwork} event bus.
 * @author rubensworks
 */
public interface ICancelableNetworkEvent<N extends INetwork<N>> extends INetworkEvent<N> {

    /**
     * Cancel this event from further processing.
     */
    public void cancel();

    /**
     * @return If this event was canceled.
     */
    public boolean isCanceled();

}
