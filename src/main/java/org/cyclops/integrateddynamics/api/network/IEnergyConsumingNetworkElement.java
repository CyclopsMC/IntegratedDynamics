package org.cyclops.integrateddynamics.api.network;

/**
 * A network element that requires power to run.
 * @author rubensworks
 */
public interface IEnergyConsumingNetworkElement<N extends INetwork<N>> extends INetworkElement<N> {

    /**
     * @return The amount of energy required per tick to work.
     */
    public int getEnergyUsage();

}
