package org.cyclops.integrateddynamics;

import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.integrateddynamics.block.BlockCableConfig;
import org.cyclops.integrateddynamics.item.ItemPartRedstoneConfig;
import org.cyclops.integrateddynamics.item.ItemWrenchConfig;

/**
 * This class holds a set of all the configs that need to be registered.
 * @author rubensworks
 */
public class Configs {

    public static void registerBlocks(ConfigHandler configHandler) {

        // Blocks
        configHandler.add(new BlockCableConfig());

        // Items
        configHandler.add(new ItemWrenchConfig());
        configHandler.add(new ItemPartRedstoneConfig());

    }

}
