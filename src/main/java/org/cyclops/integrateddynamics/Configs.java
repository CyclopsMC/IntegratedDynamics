package org.cyclops.integrateddynamics;

import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.integrateddynamics.block.*;
import org.cyclops.integrateddynamics.item.ItemFacadeConfig;
import org.cyclops.integrateddynamics.item.ItemLabellerConfig;
import org.cyclops.integrateddynamics.item.ItemVariableConfig;
import org.cyclops.integrateddynamics.item.ItemWrenchConfig;
import org.cyclops.integrateddynamics.world.biome.BiomeMeneglinConfig;

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
        configHandler.add(new BlockInvisibleLightConfig());
        configHandler.add(new BlockEnergyBatteryConfig());
        configHandler.add(new BlockCreativeEnergyBatteryConfig());
        configHandler.add(new BlockCoalGeneratorConfig());
        configHandler.add(new BlockProxyConfig());
        configHandler.add(new BlockMaterializerConfig());
        configHandler.add(new MenrilLogConfig());
        configHandler.add(new MenrilLeavesConfig());
        configHandler.add(new MenrilSaplingConfig());
        configHandler.add(new MenrilPlanksConfig());
        configHandler.add(new CrystalizedMenrilBlockConfig());
        configHandler.add(new CrystalizedMenrilBrickConfig());

        // Items
        configHandler.add(new ItemWrenchConfig());
        configHandler.add(new ItemVariableConfig());
        configHandler.add(new ItemLabellerConfig());
        configHandler.add(new ItemFacadeConfig());

        // Biomes
        configHandler.add(new BiomeMeneglinConfig());

    }

}
