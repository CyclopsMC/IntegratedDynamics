package org.cyclops.integrateddynamics.item;

import net.minecraft.world.item.Item;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Crystalized Chorus Chunk.
 * @author rubensworks
 *
 */
public class ItemCrystalizedChorusChunkConfig extends ItemConfig {

    public ItemCrystalizedChorusChunkConfig() {
        super(
                IntegratedDynamics._instance,
                "crystalized_chorus_chunk",
                eConfig -> new Item(new Item.Properties())
        );
    }

}
