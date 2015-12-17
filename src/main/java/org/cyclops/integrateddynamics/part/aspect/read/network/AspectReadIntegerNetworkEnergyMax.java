package org.cyclops.integrateddynamics.part.aspect.read.network;

import org.cyclops.integrateddynamics.api.network.IEnergyNetwork;
import org.cyclops.integrateddynamics.api.network.INetwork;

/**
 * Aspect that returns the energy capacity for a network.
 * @author rubensworks
 */
public class AspectReadIntegerNetworkEnergyMax extends AspectReadIntegerNetworkBase {

    @Override
    protected String getUnlocalizedIntegerNetworkType() {
        return "energy.max";
    }

    @Override
    protected int getValue(INetwork network) {
        return network instanceof IEnergyNetwork ? ((IEnergyNetwork) network).getMaxStoredEnergy() : 0;
    }

}
