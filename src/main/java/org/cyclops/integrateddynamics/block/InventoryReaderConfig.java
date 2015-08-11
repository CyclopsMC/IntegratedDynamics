package org.cyclops.integrateddynamics.block;

import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;

/**
 * Config for an inventory reader.
 * @author rubensworks
 */
public class InventoryReaderConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static InventoryReaderConfig _instance;

    /**
     * Make a new instance.
     */
    public InventoryReaderConfig() {
        super(
            IntegratedDynamics._instance,
            true,
            "inventoryReader",
            null,
            null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        return new IgnoredBlock(this);
    }

}
