package org.cyclops.integrateddynamics.loot.functions;

import org.cyclops.cyclopscore.config.extendedconfig.LootFunctionConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class LootFunctionCopyEnergyBatteryDataConfig extends LootFunctionConfig {
    public LootFunctionCopyEnergyBatteryDataConfig() {
        super(IntegratedDynamics._instance, "copy_energy_battery_data", LootFunctionCopyEnergyBatteryData.TYPE);
    }
}
