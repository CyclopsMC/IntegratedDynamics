package org.cyclops.integrateddynamics.block;

import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;

/**
 * Config for an world reader.
 * @author rubensworks
 */
public class WorldReaderConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static WorldReaderConfig _instance;

    /**
     * Make a new instance.
     */
    public WorldReaderConfig() {
        super(
            IntegratedDynamics._instance,
            true,
            "worldReader",
            null,
            null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        return new IgnoredBlock(this);
    }

}
