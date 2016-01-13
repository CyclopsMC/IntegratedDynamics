package org.cyclops.integrateddynamics.part.aspect.read.network;

import org.cyclops.integrateddynamics.api.network.IEnergyNetwork;
import org.cyclops.integrateddynamics.api.network.INetwork;

/**
 * Aspect that returns the energy stored in a network.
 * @author rubensworks
 */
public class AspectReadIntegerNetworkEnergyStored extends AspectReadIntegerNetworkBase {

    @Override
    protected String getUnlocalizedIntegerNetworkType() {
        return "energy.stored";
    }

    @Override
    protected int getValue(INetwork network) {
        return network instanceof IEnergyNetwork ? ((IEnergyNetwork) network).getStoredEnergy() : 0;
    }

}
