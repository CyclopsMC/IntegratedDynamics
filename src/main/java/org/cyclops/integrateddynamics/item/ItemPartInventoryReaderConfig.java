package org.cyclops.integrateddynamics.item;

import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.item.ItemPart;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.core.part.read.DefaultPartStateReader;
import org.cyclops.integrateddynamics.part.PartTypeInventoryReader;

/**
 * Config for a reader inventory part.
 * @author rubensworks
 */
public class ItemPartInventoryReaderConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemPartInventoryReaderConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemPartInventoryReaderConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "partReaderInventory",
                null,
                null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        return new ItemPart<PartTypeInventoryReader, DefaultPartStateReader<PartTypeInventoryReader>>(this, PartTypes.INVENTORY_READER);
    }
}
