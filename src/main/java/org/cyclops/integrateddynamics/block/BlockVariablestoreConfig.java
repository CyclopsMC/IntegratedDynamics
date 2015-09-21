package org.cyclops.integrateddynamics.block;

import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockVariablestore}.
 * @author rubensworks
 */
public class BlockVariablestoreConfig extends BlockContainerConfig {

    /**
     * The unique instance.
     */
    public static BlockVariablestoreConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockVariablestoreConfig() {
        super(
            IntegratedDynamics._instance,
            true,
            "variablestore",
            null,
            BlockVariablestore.class
        );
    }

}
