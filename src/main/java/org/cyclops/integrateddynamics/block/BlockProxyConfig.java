package org.cyclops.integrateddynamics.block;

import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockProxy}.
 * @author rubensworks
 */
public class BlockProxyConfig extends BlockContainerConfig {

    /**
     * The unique instance.
     */
    public static BlockProxyConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockProxyConfig() {
        super(
            IntegratedDynamics._instance,
            true,
            "proxy",
            null,
            BlockProxy.class
        );
    }
}
