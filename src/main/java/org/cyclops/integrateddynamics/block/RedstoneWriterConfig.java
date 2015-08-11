package org.cyclops.integrateddynamics.block;

import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.block.IgnoredBlockStatus;

/**
 * Config for a redstone writer.
 * @author rubensworks
 */
public class RedstoneWriterConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static RedstoneWriterConfig _instance;

    /**
     * Make a new instance.
     */
    public RedstoneWriterConfig() {
        super(
            IntegratedDynamics._instance,
            true,
            "redstoneWriter",
            null,
            null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        return new IgnoredBlockStatus(this);
    }

}
