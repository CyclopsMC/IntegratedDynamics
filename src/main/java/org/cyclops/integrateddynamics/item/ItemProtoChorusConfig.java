package org.cyclops.integrateddynamics.item;

import net.minecraft.world.item.Item;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Logic Director.
 * @author rubensworks
 *
 */
public class ItemProtoChorusConfig extends ItemConfig {

    public ItemProtoChorusConfig() {
        super(
                IntegratedDynamics._instance,
                "proto_chorus",
                eConfig -> new Item(new Item.Properties())
        );
    }

}
