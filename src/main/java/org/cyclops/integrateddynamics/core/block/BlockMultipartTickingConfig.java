package org.cyclops.integrateddynamics.core.block;

import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link org.cyclops.integrateddynamics.core.block.BlockMultipartTicking}.
 * @author rubensworks
 */
public class BlockMultipartTickingConfig extends BlockContainerConfig {

    /**
     * The unique instance.
     */
    public static BlockMultipartTickingConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockMultipartTickingConfig() {
        super(
            IntegratedDynamics._instance,
            true,
            "blockMultipart",
            null,
            BlockMultipartTicking.class
        );
    }

}
