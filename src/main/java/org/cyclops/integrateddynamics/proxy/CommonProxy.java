package org.cyclops.integrateddynamics.proxy;

import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.proxy.CommonProxyComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Common proxy
 * @author rubensworks
 */
public class CommonProxy extends CommonProxyComponent {

    @Override
    protected ModBase getMod() {
        return IntegratedDynamics._instance;
    }

}
