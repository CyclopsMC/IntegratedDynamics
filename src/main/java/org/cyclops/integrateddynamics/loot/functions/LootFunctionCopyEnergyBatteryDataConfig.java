package org.cyclops.integrateddynamics.loot.functions;

import org.cyclops.cyclopscore.config.extendedconfig.LootFunctionConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class LootFunctionCopyEnergyBatteryDataConfig extends LootFunctionConfigCommon<IModBase> {
    public LootFunctionCopyEnergyBatteryDataConfig() {
        super(IntegratedDynamics._instance, "copy_energy_battery_data", LootFunctionCopyEnergyBatteryData.CODEC);
    }
}
