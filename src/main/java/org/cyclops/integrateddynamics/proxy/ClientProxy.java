package org.cyclops.integrateddynamics.proxy;

import net.minecraftforge.client.model.ModelLoaderRegistry;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.proxy.ClientProxyComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.client.model.VariableLoader;

/**
 * Client Proxy
 * @author rubensworks
 */
public class ClientProxy extends ClientProxyComponent {

    public ClientProxy() {
        super(new CommonProxy());
    }

    @Override
    public ModBase getMod() {
        return IntegratedDynamics._instance;
    }

    @Override
    public void registerEventHooks() {
        super.registerEventHooks();
        ModelLoaderRegistry.registerLoader(new VariableLoader());
    }

}
