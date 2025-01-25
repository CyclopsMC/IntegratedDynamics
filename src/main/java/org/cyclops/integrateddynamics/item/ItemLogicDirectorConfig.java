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
public class ItemLogicDirectorConfig extends ItemConfigCommon<IModBase> {

    public ItemLogicDirectorConfig() {
        super(
                IntegratedDynamics._instance,
                "logic_director",
                (eConfig, properties) -> new Item(properties)
        );
    }

}
