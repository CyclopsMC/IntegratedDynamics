package org.cyclops.integrateddynamics.proxy;

import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.proxy.ClientProxyComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Client Proxy
 * @author rubensworks
 */
public class ClientProxy extends ClientProxyComponent {

    @Override
    protected ModBase getMod() {
        return IntegratedDynamics._instance;
    }

}
