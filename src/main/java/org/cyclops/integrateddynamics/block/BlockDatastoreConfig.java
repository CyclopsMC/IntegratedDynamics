package org.cyclops.integrateddynamics.block;

import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link BlockDatastore}.
 * @author rubensworks
 */
public class BlockDatastoreConfig extends BlockContainerConfig {

    /**
     * The unique instance.
     */
    public static BlockDatastoreConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockDatastoreConfig() {
        super(
            IntegratedDynamics._instance,
            true,
            "datastore",
            null,
            BlockDatastore.class
        );
    }

}
