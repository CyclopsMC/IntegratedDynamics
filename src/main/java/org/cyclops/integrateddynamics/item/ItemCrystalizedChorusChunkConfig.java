package org.cyclops.integrateddynamics.item;

import org.cyclops.cyclopscore.config.configurable.ConfigurableItem;
import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Crystalized Chorus Chunk.
 * @author rubensworks
 *
 */
public class ItemCrystalizedChorusChunkConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemCrystalizedChorusChunkConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemCrystalizedChorusChunkConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "crystalized_chorus_chunk",
                null,
                null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        return new ConfigurableItem(this);
    }
    
}
