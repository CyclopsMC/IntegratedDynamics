package org.cyclops.integrateddynamics.item;

import net.minecraft.world.item.Item;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for an offset enhancement.
 * @author rubensworks
 */
public class ItemEnhancementOffsetConfig extends ItemConfig {

    public ItemEnhancementOffsetConfig() {
        super(
                IntegratedDynamics._instance,
                "enhancement_offset",
                eConfig -> new ItemEnhancement(ItemEnhancement.Type.OFFSET, new Item.Properties()
                        .tab(IntegratedDynamics._instance.getDefaultItemGroup()))
        );
    }

}
