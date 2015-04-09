package org.cyclops.integrateddynamics;

import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.integrateddynamics.block.BlockMultipartTickingConfig;
import org.cyclops.integrateddynamics.item.ItemPartRedstoneConfig;

/**
 * This class holds a set of all the configs that need to be registered.
 * @author rubensworks
 */
public class Configs {

    public static void registerBlocks(ConfigHandler configHandler) {

        // Blocks
        configHandler.add(new BlockMultipartTickingConfig());

        // Items
        configHandler.add(new ItemPartRedstoneConfig());

    }

}
