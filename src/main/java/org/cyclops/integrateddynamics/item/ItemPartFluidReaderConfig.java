package org.cyclops.integrateddynamics.item;

import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.item.ItemPart;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.core.part.read.DefaultPartStateReader;
import org.cyclops.integrateddynamics.part.PartTypeFluidReader;

/**
 * Config for a reader fluid part.
 * @author rubensworks
 */
public class ItemPartFluidReaderConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemPartFluidReaderConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemPartFluidReaderConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "partReaderFluid",
                null,
                null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        return new ItemPart<PartTypeFluidReader, DefaultPartStateReader<PartTypeFluidReader>>(this, PartTypes.FLUID_READER);
    }
}
