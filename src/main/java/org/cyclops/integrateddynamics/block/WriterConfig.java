package org.cyclops.integrateddynamics.block;

import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.block.IgnoredBlockStatus;

/**
 * Config for a writer.
 * @author rubensworks
 */
public class WriterConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static WriterConfig _instance;

    /**
     * Make a new instance.
     */
    public WriterConfig() {
        super(
            IntegratedDynamics._instance,
            true,
            "writer",
            null,
            null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        return new IgnoredBlockStatus(this);
    }

}
