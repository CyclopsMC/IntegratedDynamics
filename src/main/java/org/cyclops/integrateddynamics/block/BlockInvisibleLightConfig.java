package org.cyclops.integrateddynamics.block;

import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockInvisibleLight}.
 * @author rubensworks
 */
public class BlockInvisibleLightConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static BlockInvisibleLightConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockInvisibleLightConfig() {
        super(
            IntegratedDynamics._instance,
            true,
            "invisibleLight",
            null,
            BlockInvisibleLight.class
        );
    }

}
