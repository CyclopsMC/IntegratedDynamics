package org.cyclops.integrateddynamics.item;

import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.item.ItemPart;
import org.cyclops.integrateddynamics.core.part.DefaultPartState;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.PartTypeRedstoneReader;

/**
 * Config for a reader redstone part.
 * @author rubensworks
 */
public class ItemPartRedstoneReaderConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemPartRedstoneReaderConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemPartRedstoneReaderConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "partReaderRedstone",
                null,
                null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        return new ItemPart<PartTypeRedstoneReader, DefaultPartState<PartTypeRedstoneReader>>(this, PartTypes.REDSTONE_READER);
    }
}
