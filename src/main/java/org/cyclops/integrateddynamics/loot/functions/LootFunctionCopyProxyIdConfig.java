package org.cyclops.integrateddynamics.loot.functions;

import org.cyclops.cyclopscore.config.extendedconfig.LootFunctionConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class LootFunctionCopyProxyIdConfig extends LootFunctionConfigCommon<IModBase> {
    public LootFunctionCopyProxyIdConfig() {
        super(IntegratedDynamics._instance, "copy_proxy_id", LootFunctionCopyProxyId.CODEC);
    }
}
