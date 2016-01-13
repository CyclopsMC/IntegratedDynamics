package org.cyclops.integrateddynamics.part.aspect.read.network;

import org.cyclops.integrateddynamics.api.network.IEnergyNetwork;
import org.cyclops.integrateddynamics.api.network.INetwork;

/**
 * Aspect that counts the amount of energy batteries in a network.
 * @author rubensworks
 */
public class AspectReadIntegerNetworkEnergyBatteryCount extends AspectReadIntegerNetworkBase {

    @Override
    protected String getUnlocalizedIntegerNetworkType() {
        return "energy.batterycount";
    }

    @Override
    protected int getValue(INetwork network) {
        return network instanceof IEnergyNetwork ? ((IEnergyNetwork) network).getEnergyBatteries().size() : 0;
    }

}
