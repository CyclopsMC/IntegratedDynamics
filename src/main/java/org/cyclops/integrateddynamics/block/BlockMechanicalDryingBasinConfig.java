package org.cyclops.integrateddynamics.block;

import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.ConfigurableTypeCategory;
import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockMechanicalDryingBasin}.
 * @author rubensworks
 */
public class BlockMechanicalDryingBasinConfig extends BlockContainerConfig {

    /**
     * The unique instance.
     */
    public static BlockMechanicalDryingBasinConfig _instance;

    /**
     * The energy capacity of a mechanical drying basin.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The energy capacity of a mechanical drying basin.", minimalValue = 0)
    public static int capacity = 100000;

    /**
     * The energy consumption rate.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The energy consumption rate.", minimalValue = 0)
    public static int consumptionRate = 80;

    /**
     * Make a new instance.
     */
    public BlockMechanicalDryingBasinConfig() {
        super(
            IntegratedDynamics._instance,
            true,
            "mechanical_drying_basin",
            null,
            BlockMechanicalDryingBasin.class
        );
    }
}
