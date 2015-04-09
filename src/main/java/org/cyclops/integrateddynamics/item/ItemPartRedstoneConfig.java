package org.cyclops.integrateddynamics.item;

import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.item.ItemPart;
import org.cyclops.integrateddynamics.parts.PartRedstone;
import org.cyclops.integrateddynamics.parts.Parts;

/**
 * Config for a redstone part.
 * @author rubensworks
 */
public class ItemPartRedstoneConfig extends ItemConfig {

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
        return new ItemPart<PartRedstone, PartRedstone.PartRedstoneState>(this, Parts.REDSTONE.getPart());
    }
}
