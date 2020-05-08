package org.cyclops.integrateddynamics.block;

import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.ConfigurableTypeCategory;
import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockMaterializer}.
 * @author rubensworks
 */
public class BlockMaterializerConfig extends BlockContainerConfig {

    /**
     * The unique instance.
     */
    public static BlockMaterializerConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockMaterializerConfig() {
        super(
            IntegratedDynamics._instance,
            true,
            "materializer",
            null,
            BlockMaterializer.class
        );
    }
}
