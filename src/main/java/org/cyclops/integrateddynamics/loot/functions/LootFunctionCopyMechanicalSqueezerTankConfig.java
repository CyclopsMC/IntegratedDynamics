package org.cyclops.integrateddynamics.loot.functions;

import org.cyclops.cyclopscore.config.extendedconfig.LootFunctionConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class LootFunctionCopyMechanicalSqueezerTankConfig extends LootFunctionConfigCommon<IModBase> {
    public LootFunctionCopyMechanicalSqueezerTankConfig() {
        super(IntegratedDynamics._instance, "copy_mechanical_squeezer_tank", LootFunctionCopyMechanicalSqueezerTank.CODEC);
    }
}
