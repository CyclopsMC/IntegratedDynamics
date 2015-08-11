package org.cyclops.integrateddynamics;

import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.integrateddynamics.block.*;
import org.cyclops.integrateddynamics.item.*;

/**
 * This class holds a set of all the configs that need to be registered.
 * @author rubensworks
 */
public class Configs {

    public static void registerBlocks(ConfigHandler configHandler) {

        // Blocks
        configHandler.add(new BlockCableConfig());
        configHandler.add(new BlockDatastoreConfig());
        configHandler.add(new BlockLogicProgrammerConfig());

        // Part blocks
        configHandler.add(new RedstoneReaderConfig());
        configHandler.add(new RedstoneWriterConfig());
        configHandler.add(new InventoryReaderConfig());

        // Items
        configHandler.add(new ItemWrenchConfig());
        configHandler.add(new ItemVariableConfig());
        configHandler.add(new ItemLabellerConfig());

        // Part items
        configHandler.add(new ItemPartRedstoneReaderConfig());
        configHandler.add(new ItemPartRedstoneWriterConfig());
        configHandler.add(new ItemPartInventoryReaderConfig());

    }

}
