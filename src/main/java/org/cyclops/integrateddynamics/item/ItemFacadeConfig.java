package org.cyclops.integrateddynamics.item;

import net.minecraft.item.Item;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the facade.
 * @author rubensworks
 */
public class ItemFacadeConfig extends ItemConfig {

    public ItemFacadeConfig() {
        super(
                IntegratedDynamics._instance,
                "facade",
                eConfig -> new ItemFacade(new Item.Properties()
                        .group(IntegratedDynamics._instance.getDefaultItemGroup()))
        );
    }

}
