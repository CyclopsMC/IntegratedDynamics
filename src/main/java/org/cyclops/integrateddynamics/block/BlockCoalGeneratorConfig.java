package org.cyclops.integrateddynamics.block;

import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockCoalGenerator}.
 * @author rubensworks
 */
public class BlockCoalGeneratorConfig extends BlockContainerConfig {

    /**
     * The unique instance.
     */
    public static BlockCoalGeneratorConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockCoalGeneratorConfig() {
        super(
            IntegratedDynamics._instance,
            true,
            "coalGenerator",
            null,
            BlockCoalGenerator.class
        );
    }
}
