package org.cyclops.integrateddynamics;

import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.integrateddynamics.block.*;
import org.cyclops.integrateddynamics.capability.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.PartContainerConfig;
import org.cyclops.integrateddynamics.fluid.FluidMenrilResinConfig;
import org.cyclops.integrateddynamics.item.*;
import org.cyclops.integrateddynamics.world.biome.BiomeMeneglinConfig;

/**
 * This class holds a set of all the configs that need to be registered.
 * @author rubensworks
 */
public class Configs {

    public static void registerBlocks(ConfigHandler configHandler) {

        // Capabilities
        configHandler.add(new PartContainerConfig());
        configHandler.add(new NetworkElementProviderConfig());

        // Fluids
        configHandler.add(new FluidMenrilResinConfig());

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
        configHandler.add(new BlockMenrilLogConfig());
        configHandler.add(new BlockMenrilLogFilledConfig());
        configHandler.add(new BlockMenrilLeavesConfig());
        configHandler.add(new BlockMenrilSaplingConfig());
        configHandler.add(new BlockMenrilPlanksConfig());
        configHandler.add(new BlockCrystalizedMenrilBlockConfig());
        configHandler.add(new BlockCrystalizedMenrilBrickConfig());
        configHandler.add(new BlockFluidMenrilResinConfig());
        configHandler.add(new BlockDryingBasinConfig());
        configHandler.add(new BlockSqueezerConfig());
        configHandler.add(new BlockMenrilTorchConfig());

        // Items
        configHandler.add(new ItemWrenchConfig());
        configHandler.add(new ItemVariableConfig());
        configHandler.add(new ItemLabellerConfig());
        configHandler.add(new ItemFacadeConfig());
        configHandler.add(new ItemCrystalizedMenrilChunkConfig());
        configHandler.add(new ItemVariableTransformerConfig());
        configHandler.add(new ItemMenrilBerriesConfig());

        // Biomes
        configHandler.add(new BiomeMeneglinConfig());

    }

}
