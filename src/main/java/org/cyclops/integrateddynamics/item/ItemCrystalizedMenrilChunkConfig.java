package org.cyclops.integrateddynamics.item;

import net.minecraft.world.item.Item;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Crystalized Menril Chunk.
 * @author rubensworks
 *
 */
public class ItemCrystalizedMenrilChunkConfig extends ItemConfigCommon<IModBase> {

    public ItemCrystalizedMenrilChunkConfig() {
        super(
                IntegratedDynamics._instance,
                "crystalized_menril_chunk",
                (eConfig, properties) -> new Item(properties)
        );
    }

}
