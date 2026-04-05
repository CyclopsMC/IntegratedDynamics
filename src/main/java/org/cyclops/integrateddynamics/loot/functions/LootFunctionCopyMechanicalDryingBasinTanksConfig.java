package org.cyclops.integrateddynamics.loot.functions;

import org.cyclops.cyclopscore.config.extendedconfig.LootFunctionConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class LootFunctionCopyMechanicalDryingBasinTanksConfig extends LootFunctionConfigCommon<IModBase> {
    public LootFunctionCopyMechanicalDryingBasinTanksConfig() {
        super(IntegratedDynamics._instance, "copy_mechanical_drying_basin_tanks", LootFunctionCopyMechanicalDryingBasinTanks.CODEC);
    }
}
