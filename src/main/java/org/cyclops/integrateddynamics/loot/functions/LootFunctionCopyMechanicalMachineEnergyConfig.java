package org.cyclops.integrateddynamics.loot.functions;

import org.cyclops.cyclopscore.config.extendedconfig.LootFunctionConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class LootFunctionCopyMechanicalMachineEnergyConfig extends LootFunctionConfig {
    public LootFunctionCopyMechanicalMachineEnergyConfig() {
        super(IntegratedDynamics._instance, "copy_mechanical_machine_energy", LootFunctionCopyMechanicalMachineEnergy.TYPE);
    }
}
