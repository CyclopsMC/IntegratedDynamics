package org.cyclops.integrateddynamics.api.network.event;

import org.cyclops.integrateddynamics.api.network.INetwork;

/**
 * An event posted in the {@link INetwork} event bus.
 * @author rubensworks
 */
public interface ICancelableNetworkEvent extends INetworkEvent {

    /**
     * Cancel this event from further processing.
     */
    public void cancel();

    /**
     * @return If this event was canceled.
     */
    public boolean isCanceled();

}
