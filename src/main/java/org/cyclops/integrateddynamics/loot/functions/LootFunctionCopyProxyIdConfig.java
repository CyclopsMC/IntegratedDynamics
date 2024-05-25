package org.cyclops.integrateddynamics.loot.functions;

import org.cyclops.cyclopscore.config.extendedconfig.LootFunctionConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class LootFunctionCopyProxyIdConfig extends LootFunctionConfig {
    public LootFunctionCopyProxyIdConfig() {
        super(IntegratedDynamics._instance, "copy_proxy_id", LootFunctionCopyProxyId.TYPE);
    }
}
