package org.cyclops.integrateddynamics.item;

import net.minecraft.world.item.Item;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Logic Director.
 * @author rubensworks
 *
 */
public class ItemLogicDirectorConfig extends ItemConfig {

    public ItemLogicDirectorConfig() {
        super(
                IntegratedDynamics._instance,
                "logic_director",
                eConfig -> new Item(new Item.Properties()
                        .tab(IntegratedDynamics._instance.getDefaultItemGroup()))
        );
    }
    
}
