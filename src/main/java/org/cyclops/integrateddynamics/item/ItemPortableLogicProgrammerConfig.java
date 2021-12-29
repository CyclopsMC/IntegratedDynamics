package org.cyclops.integrateddynamics.item;

import net.minecraft.world.item.Item;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the portable logic programmer.
 * @author rubensworks
 */
public class ItemPortableLogicProgrammerConfig extends ItemConfig {

    public ItemPortableLogicProgrammerConfig() {
        super(
                IntegratedDynamics._instance,
                "portable_logic_programmer",
                eConfig -> new ItemPortableLogicProgrammer(new Item.Properties()
                        .tab(IntegratedDynamics._instance.getDefaultItemGroup()))
        );
    }

}
