package org.cyclops.integrateddynamics.block;

import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.block.BlockMultipartTicking;

/**
 * Config for {@link BlockMultipartTicking}.
 * @author rubensworks
 */
public class BlockCableConfig extends BlockContainerConfig {

    /**
     * The unique instance.
     */
    public static BlockCableConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockCableConfig() {
        super(
            IntegratedDynamics._instance,
            true,
            "cable",
            null,
            BlockCable.class
        );
    }

}
