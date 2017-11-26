package org.cyclops.integrateddynamics;

import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.integrateddynamics.block.*;
import org.cyclops.integrateddynamics.capability.cable.CableConfig;
import org.cyclops.integrateddynamics.capability.cable.CableFakeableConfig;
import org.cyclops.integrateddynamics.capability.dynamiclight.DynamicLightConfig;
import org.cyclops.integrateddynamics.capability.dynamicredstone.DynamicRedstoneConfig;
import org.cyclops.integrateddynamics.capability.facadeable.FacadeableConfig;
import org.cyclops.integrateddynamics.capability.network.EnergyNetworkConfig;
import org.cyclops.integrateddynamics.capability.network.NetworkCarrierConfig;
import org.cyclops.integrateddynamics.capability.network.PartNetworkConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.partcontainer.PartContainerConfig;
import org.cyclops.integrateddynamics.capability.path.PathElementConfig;
import org.cyclops.integrateddynamics.capability.variablecontainer.VariableContainerConfig;
import org.cyclops.integrateddynamics.capability.variablefacade.VariableFacadeHolderConfig;
import org.cyclops.integrateddynamics.entity.item.EntityItemTargettedConfig;
import org.cyclops.integrateddynamics.fluid.FluidLiquidChorusConfig;
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
        configHandler.add(new DynamicLightConfig());
        configHandler.add(new DynamicRedstoneConfig());
        configHandler.add(new FacadeableConfig());
        configHandler.add(new VariableContainerConfig());
        configHandler.add(new CableConfig());
        configHandler.add(new CableFakeableConfig());
        configHandler.add(new NetworkCarrierConfig());
        configHandler.add(new PathElementConfig());
        configHandler.add(new VariableFacadeHolderConfig());
        configHandler.add(new PartNetworkConfig());
        configHandler.add(new EnergyNetworkConfig());

        // Fluids
        configHandler.add(new FluidMenrilResinConfig());
        configHandler.add(new FluidLiquidChorusConfig());

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
        configHandler.add(new BlockMenrilTorchStoneConfig());
        configHandler.add(new BlockMenrilPlanksStairsConfig());
        configHandler.add(new BlockCrystalizedMenrilBlockStairsConfig());
        configHandler.add(new BlockCrystalizedMenrilBrickStairsConfig());
        configHandler.add(new BlockDelayConfig());
        configHandler.add(new BlockFluidLiquidChorusConfig());
        configHandler.add(new BlockCrystalizedChorusBlockConfig());
        configHandler.add(new BlockCrystalizedChorusBrickConfig());
        configHandler.add(new BlockCrystalizedChorusBlockStairsConfig());
        configHandler.add(new BlockCrystalizedChorusBrickStairsConfig());
        configHandler.add(new BlockMechanicalSqueezerConfig());
        configHandler.add(new BlockMechanicalDryingBasinConfig());

        // Items
        configHandler.add(new ItemWrenchConfig());
        configHandler.add(new ItemVariableConfig());
        configHandler.add(new ItemLabellerConfig());
        configHandler.add(new ItemFacadeConfig());
        configHandler.add(new ItemCrystalizedMenrilChunkConfig());
        configHandler.add(new ItemVariableTransformerConfig());
        configHandler.add(new ItemMenrilBerriesConfig());
        configHandler.add(new ItemPortableLogicProgrammerConfig());
        configHandler.add(new ItemOnTheDynamicsOfIntegrationConfig());
        configHandler.add(new ItemCrystalizedChorusChunkConfig());
        configHandler.add(new ItemLogicDirectorConfig());

        // Biomes
        configHandler.add(new BiomeMeneglinConfig());

        // Entities
        configHandler.add(new EntityItemTargettedConfig());
    }

}
