package org.cyclops.integrateddynamics.loot.functions;

import org.cyclops.cyclopscore.config.extendedconfig.LootFunctionConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class LootFunctionCopyMechanicalMachineEnergyConfig extends LootFunctionConfigCommon<IModBase> {
    public LootFunctionCopyMechanicalMachineEnergyConfig() {
        super(IntegratedDynamics._instance, "copy_mechanical_machine_energy", LootFunctionCopyMechanicalMachineEnergy.CODEC);
    }
}
