package org.cyclops.integrateddynamics.part.aspect.read.network;

import org.cyclops.integrateddynamics.api.network.IEnergyNetwork;
import org.cyclops.integrateddynamics.api.network.INetwork;

/**
 * Aspect that counts the amount of energy batteries in a network.
 * @author rubensworks
 */
public class AspectReadIntegerNetworkEnergyConsumptionRate extends AspectReadIntegerNetworkBase {

    @Override
    protected String getUnlocalizedIntegerNetworkType() {
        return "energy.consumptionrate";
    }

    @Override
    protected int getValue(INetwork network) {
        return network instanceof IEnergyNetwork ? ((IEnergyNetwork) network).getConsumptionRate() : 0;
    }

}
