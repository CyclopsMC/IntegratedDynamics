package org.cyclops.integrateddynamics.item;

import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.item.ItemPart;
import org.cyclops.integrateddynamics.core.part.DefaultPartState;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.PartTypeRedstoneWriter;

/**
 * Config for a writer redstone part.
 * @author rubensworks
 */
public class ItemPartRedstoneWriterConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemPartRedstoneWriterConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemPartRedstoneWriterConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "partWriterRedstone",
                null,
                null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        return new ItemPart<PartTypeRedstoneWriter, DefaultPartState<PartTypeRedstoneWriter>>(this, PartTypes.REDSTONE_WRITER);
    }
}
