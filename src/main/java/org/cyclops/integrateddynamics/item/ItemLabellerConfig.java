package org.cyclops.integrateddynamics.item;

import net.minecraft.item.Item;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the labeller.
 * @author rubensworks
 */
public class ItemLabellerConfig extends ItemConfig {

    public ItemLabellerConfig() {
        super(
                IntegratedDynamics._instance,
                "labeller",
                eConfig -> new ItemLabeller(new Item.Properties()
                        .group(IntegratedDynamics._instance.getDefaultItemGroup()))
        );
    }

}
