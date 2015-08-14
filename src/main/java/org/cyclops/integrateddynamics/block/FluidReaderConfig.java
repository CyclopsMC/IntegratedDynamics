package org.cyclops.integrateddynamics.block;

import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;

/**
 * Config for an fluid reader.
 * @author rubensworks
 */
public class FluidReaderConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static FluidReaderConfig _instance;

    /**
     * Make a new instance.
     */
    public FluidReaderConfig() {
        super(
            IntegratedDynamics._instance,
            true,
            "fluidReader",
            null,
            null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        return new IgnoredBlock(this);
    }

}
