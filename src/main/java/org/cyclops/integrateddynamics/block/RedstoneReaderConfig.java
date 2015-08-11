package org.cyclops.integrateddynamics.block;

import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;

/**
 * Config for a redstone reader.
 * @author rubensworks
 */
public class RedstoneReaderConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static RedstoneReaderConfig _instance;

    /**
     * Make a new instance.
     */
    public RedstoneReaderConfig() {
        super(
            IntegratedDynamics._instance,
            true,
            "redstoneReader",
            null,
            null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        return new IgnoredBlock(this);
    }

}
