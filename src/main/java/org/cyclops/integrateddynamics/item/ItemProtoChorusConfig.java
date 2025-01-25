package org.cyclops.integrateddynamics.item;

import net.minecraft.world.item.Item;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Logic Director.
 * @author rubensworks
 *
 */
public class ItemProtoChorusConfig extends ItemConfigCommon<IModBase> {

    public ItemProtoChorusConfig() {
        super(
                IntegratedDynamics._instance,
                "proto_chorus",
                (eConfig, properties) -> new Item(properties)
        );
    }

}
