package org.cyclops.integrateddynamics.item;

import net.minecraft.world.item.Item;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Crystalized Chorus Chunk.
 * @author rubensworks
 *
 */
public class ItemCrystalizedChorusChunkConfig extends ItemConfigCommon<IModBase> {

    public ItemCrystalizedChorusChunkConfig() {
        super(
                IntegratedDynamics._instance,
                "crystalized_chorus_chunk",
                (eConfig, properties) -> new Item(properties)
        );
    }

}
