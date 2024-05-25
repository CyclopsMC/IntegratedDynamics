package org.cyclops.integrateddynamics.loot.functions;

import org.cyclops.cyclopscore.config.extendedconfig.LootFunctionConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class LootFunctionCopyMechanicalSqueezerTankConfig extends LootFunctionConfig {
    public LootFunctionCopyMechanicalSqueezerTankConfig() {
        super(IntegratedDynamics._instance, "copy_mechanical_squeezer_tank", LootFunctionCopyMechanicalSqueezerTank.TYPE);
    }
}
