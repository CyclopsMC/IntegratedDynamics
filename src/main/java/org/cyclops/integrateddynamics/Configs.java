package org.cyclops.integrateddynamics;

import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.integrateddynamics.block.BlockCableConfig;
import org.cyclops.integrateddynamics.block.ReaderConfig;
import org.cyclops.integrateddynamics.block.WriterConfig;
import org.cyclops.integrateddynamics.item.ItemPartRedstoneReaderConfig;
import org.cyclops.integrateddynamics.item.ItemPartRedstoneWriterConfig;
import org.cyclops.integrateddynamics.item.ItemVariableConfig;
import org.cyclops.integrateddynamics.item.ItemWrenchConfig;

/**
 * This class holds a set of all the configs that need to be registered.
 * @author rubensworks
 */
public class Configs {

    public static void registerBlocks(ConfigHandler configHandler) {

        // Blocks
        configHandler.add(new BlockCableConfig());
        configHandler.add(new ReaderConfig());
        configHandler.add(new WriterConfig());

        // Items
        configHandler.add(new ItemWrenchConfig());
        configHandler.add(new ItemVariableConfig());
        configHandler.add(new ItemPartRedstoneReaderConfig());
        configHandler.add(new ItemPartRedstoneWriterConfig());

    }

}
