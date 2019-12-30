package org.cyclops.integrateddynamics.item;

import net.minecraft.item.Item;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for a wrench.
 * @author rubensworks
 */
public class ItemWrenchConfig extends ItemConfig {

    public ItemWrenchConfig() {
        super(
                IntegratedDynamics._instance,
                "wrench",
                eConfig -> new ItemWrench(new Item.Properties()
                        .maxStackSize(1)
                        .group(IntegratedDynamics._instance.getDefaultItemGroup()))
        );
    }

}
