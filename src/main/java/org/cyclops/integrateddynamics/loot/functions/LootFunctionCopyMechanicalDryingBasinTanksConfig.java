package org.cyclops.integrateddynamics.loot.functions;

import org.cyclops.cyclopscore.config.extendedconfig.LootFunctionConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class LootFunctionCopyMechanicalDryingBasinTanksConfig extends LootFunctionConfig {
    public LootFunctionCopyMechanicalDryingBasinTanksConfig() {
        super(IntegratedDynamics._instance, "copy_mechanical_drying_basin_tanks", LootFunctionCopyMechanicalDryingBasinTanks.TYPE);
    }
}
