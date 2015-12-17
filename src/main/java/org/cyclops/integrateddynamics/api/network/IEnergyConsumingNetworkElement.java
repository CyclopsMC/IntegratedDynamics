package org.cyclops.integrateddynamics.api.network;

/**
 * An energy consuming network element.
 * @author rubensworks
 */
public interface IEnergyConsumingNetworkElement<N extends INetwork> extends INetworkElement<N> {

    /**
     * @return The energy consumption rate of this part for the given state.
     */
    public int getConsumptionRate();

}
