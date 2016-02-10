package org.cyclops.integrateddynamics.item;

import org.cyclops.cyclopscore.config.configurable.ConfigurableItem;
import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Crystalized Menril Chunk.
 * @author rubensworks
 *
 */
public class ItemCrystalizedMenrilChunkConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemCrystalizedMenrilChunkConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemCrystalizedMenrilChunkConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "crystalizedMenrilChunk",
                null,
                null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        return new ConfigurableItem(this);
    }
    
}
