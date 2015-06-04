package org.cyclops.integrateddynamics.block;

import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;

/**
 * Config for a reader.
 * @author rubensworks
 */
public class ReaderConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static ReaderConfig _instance;

    /**
     * Make a new instance.
     */
    public ReaderConfig() {
        super(
            IntegratedDynamics._instance,
            true,
            "reader",
            null,
            null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        return new IgnoredBlock(this);
    }

}
