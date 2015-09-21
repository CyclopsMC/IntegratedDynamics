package org.cyclops.integrateddynamics;

import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.integrateddynamics.block.BlockCableConfig;
import org.cyclops.integrateddynamics.block.BlockLogicProgrammerConfig;
import org.cyclops.integrateddynamics.block.BlockVariablestoreConfig;
import org.cyclops.integrateddynamics.item.ItemFacadeConfig;
import org.cyclops.integrateddynamics.item.ItemLabellerConfig;
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
        configHandler.add(new BlockVariablestoreConfig());
        configHandler.add(new BlockLogicProgrammerConfig());

        // Items
        configHandler.add(new ItemWrenchConfig());
        configHandler.add(new ItemVariableConfig());
        configHandler.add(new ItemLabellerConfig());
        configHandler.add(new ItemFacadeConfig());

    }

}
