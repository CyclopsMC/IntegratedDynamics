package org.cyclops.integrateddynamics.part.aspect.read.network;

import org.cyclops.integrateddynamics.api.network.INetwork;

/**
 * Aspect that can counts the amount of elements in a network.
 * @author rubensworks
 */
public class AspectReadIntegerNetworkElementCount extends AspectReadIntegerNetworkBase {

    @Override
    protected String getUnlocalizedIntegerNetworkType() {
        return "elementcount";
    }

    @Override
    protected int getValue(INetwork network) {
        return network.getElements().size();
    }

}
