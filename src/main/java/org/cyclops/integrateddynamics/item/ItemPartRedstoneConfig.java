package org.cyclops.integrateddynamics.item;

import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.item.ItemPart;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.PartTypeRedstone;

/**
 * Config for a redstone part.
 * @author rubensworks
 */
public class ItemPartRedstoneConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemPartRedstoneConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemPartRedstoneConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "partRedstone",
                null,
                null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        return new ItemPart<PartTypeRedstone, PartTypeRedstone.PartRedstoneState>(this, PartTypes.REDSTONE.getPart());
    }
}
