package org.cyclops.integrateddynamics.block;

import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockEnergyBattery}.
 * @author rubensworks
 */
public class BlockEnergyBatteryConfig extends BlockContainerConfig {

    /**
     * The unique instance.
     */
    public static BlockEnergyBatteryConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockEnergyBatteryConfig() {
        super(
            IntegratedDynamics._instance,
            true,
            "energyBattery",
            null,
            BlockEnergyBattery.class
        );
    }

}
