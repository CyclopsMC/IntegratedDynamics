package org.cyclops.integrateddynamics.item;

import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.item.ItemPart;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.core.part.read.DefaultPartStateReader;
import org.cyclops.integrateddynamics.part.PartTypeWorldReader;

/**
 * Config for a reader world part.
 * @author rubensworks
 */
public class ItemPartWorldReaderConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemPartWorldReaderConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemPartWorldReaderConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "partReaderWorld",
                null,
                null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        return new ItemPart<PartTypeWorldReader, DefaultPartStateReader<PartTypeWorldReader>>(this, PartTypes.WORLD_READER);
    }
}
