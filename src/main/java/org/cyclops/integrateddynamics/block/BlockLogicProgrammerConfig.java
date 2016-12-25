package org.cyclops.integrateddynamics.block;

import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockLogicProgrammer}.
 * @author rubensworks
 */
public class BlockLogicProgrammerConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static BlockLogicProgrammerConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockLogicProgrammerConfig() {
        super(
            IntegratedDynamics._instance,
            true,
            "logic_programmer",
            null,
            BlockLogicProgrammer.class
        );
    }

}
