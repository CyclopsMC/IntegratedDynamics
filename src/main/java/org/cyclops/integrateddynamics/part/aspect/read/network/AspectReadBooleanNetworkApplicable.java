package org.cyclops.integrateddynamics.part.aspect.read.network;

import org.cyclops.integrateddynamics.api.network.INetwork;

/**
 * Aspect that checks if the target has a network.
 * @author rubensworks
 */
public class AspectReadBooleanNetworkApplicable extends AspectReadBooleanNetworkBase {

    @Override
    protected String getUnlocalizedBooleanNetworkType() {
        return "applicable";
    }

    @Override
    protected boolean getValue(INetwork network) {
        return true;
    }

}
