package org.cyclops.integrateddynamics.api.network;

/**
 * An energy consuming network element.
 * @author rubensworks
 */
public interface IEnergyConsumingNetworkElement extends INetworkElement {

    /**
     * @return The energy consumption rate of this part for the given state.
     */
    public int getConsumptionRate();

    /**
     * Called after the element was updated or not.
     * If the update was not called, this can be because the network did not contain
     * enough energy to let this element work.
     * @param network The network.
     * @param updated If the {@link INetworkElement#update(INetwork)} was called.
     */
    public void postUpdate(INetwork network, boolean updated);

}
