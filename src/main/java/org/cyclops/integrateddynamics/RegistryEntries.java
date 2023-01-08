package org.cyclops.integrateddynamics;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraftforge.registries.ObjectHolder;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.block.BlockDryingBasin;
import org.cyclops.integrateddynamics.block.BlockEnergyBattery;
import org.cyclops.integrateddynamics.block.BlockFluidLiquidChorus;
import org.cyclops.integrateddynamics.block.BlockFluidMenrilResin;
import org.cyclops.integrateddynamics.block.BlockMechanicalDryingBasin;
import org.cyclops.integrateddynamics.block.BlockMechanicalSqueezer;
import org.cyclops.integrateddynamics.block.BlockSqueezer;
import org.cyclops.integrateddynamics.blockentity.*;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMultipartTicking;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerAspectSettings;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerPartSettings;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainer;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeDryingBasin;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeEnergyContainerCombination;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeMechanicalDryingBasin;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeMechanicalSqueezer;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeNbtClear;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeSqueezer;
import org.cyclops.integrateddynamics.inventory.container.*;
import org.cyclops.integrateddynamics.item.ItemFacade;
import org.cyclops.integrateddynamics.item.ItemVariable;
import org.cyclops.integrateddynamics.recipe.ItemFacadeRecipe;
import org.cyclops.integrateddynamics.recipe.ItemVariableCopyRecipe;
import org.cyclops.integrateddynamics.world.gen.foliageplacer.FoliagePlacerMenril;
import org.cyclops.integrateddynamics.world.gen.trunkplacer.TrunkPlacerMenril;

/**
 * Referenced registry entries.
 * @author rubensworks
 */
public class RegistryEntries {

    @ObjectHolder(registryName = "item", value = "integrateddynamics:bucket_liquid_chorus")
    public static final Item ITEM_BUCKET_LIQUID_CHORUS = null;
    @ObjectHolder(registryName = "item", value = "integrateddynamics:bucket_menril_resin")
    public static final Item ITEM_BUCKET_MENRIL_RESIN = null;
    @ObjectHolder(registryName = "item", value = "integrateddynamics:cable")
    public static final Item ITEM_CABLE = null;
    @ObjectHolder(registryName = "item", value = "integrateddynamics:delay")
    public static final Item ITEM_DELAY = null;
    @ObjectHolder(registryName = "item", value = "integrateddynamics:energy_battery")
    public static final ItemBlockEnergyContainer ITEM_ENERGY_BATTERY = null;
    @ObjectHolder(registryName = "item", value = "integrateddynamics:facade")
    public static final ItemFacade ITEM_FACADE = null;
    @ObjectHolder(registryName = "item", value = "integrateddynamics:on_the_dynamics_of_integration")
    public static final Item ITEM_ON_THE_DYNAMICS_OF_INTEGRATION = null;
    @ObjectHolder(registryName = "item", value = "integrateddynamics:labeller")
    public static final Item ITEM_LABELLER = null;
    @ObjectHolder(registryName = "item", value = "integrateddynamics:portable_logic_programmer")
    public static final Item ITEM_PORTABLE_LOGIC_PROGRAMMER = null;
    @ObjectHolder(registryName = "item", value = "integrateddynamics:proxy")
    public static final Item ITEM_PROXY = null;
    @ObjectHolder(registryName = "item", value = "integrateddynamics:variable")
    public static final ItemVariable ITEM_VARIABLE = null;
    @ObjectHolder(registryName = "item", value = "integrateddynamics:wrench")
    public static final Item ITEM_WRENCH = null;
    @ObjectHolder(registryName = "item", value = "integrateddynamics:menril_torch")
    public static final Item ITEM_MENRIL_TORCH = null;
    @ObjectHolder(registryName = "item", value = "integrateddynamics:menril_torch_stone")
    public static final Item ITEM_MENRIL_TORCH_STONE = null;

    @ObjectHolder(registryName = "block", value = "integrateddynamics:cable")
    public static final BlockCable BLOCK_CABLE = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:coal_generator")
    public static final Block BLOCK_COAL_GENERATOR = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:crystalized_menril_brick")
    public static final Block BLOCK_CRYSTALIZED_MENRIL_BRICK = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:crystalized_menril_block")
    public static final Block BLOCK_CRYSTALIZED_MENRIL_BLOCK = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:crystalized_chorus_brick")
    public static final Block BLOCK_CRYSTALIZED_CHORUS_BRICK = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:crystalized_chorus_block")
    public static final Block BLOCK_CRYSTALIZED_CHORUS_BLOCK = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:delay")
    public static final Block BLOCK_DELAY = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:drying_basin")
    public static final BlockDryingBasin BLOCK_DRYING_BASIN = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:energy_battery")
    public static final BlockEnergyBattery BLOCK_ENERGY_BATTERY = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:block_liquid_chorus")
    public static final BlockFluidLiquidChorus BLOCK_FLUID_LIQUID_CHORUS = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:block_menril_resin")
    public static final BlockFluidMenrilResin BLOCK_FLUID_MENRIL_RESIN = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:invisible_light")
    public static final Block BLOCK_INVISIBLE_LIGHT = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:logic_programmer")
    public static final Block BLOCK_LOGIC_PROGRAMMER = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:materializer")
    public static final Block BLOCK_MATERIALIZER = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:mechanical_drying_basin")
    public static final BlockMechanicalDryingBasin BLOCK_MECHANICAL_DRYING_BASIN = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:mechanical_squeezer")
    public static final BlockMechanicalSqueezer BLOCK_MECHANICAL_SQUEEZER = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:menril_leaves")
    public static final Block BLOCK_MENRIL_LEAVES = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:menril_log")
    public static final Block BLOCK_MENRIL_LOG = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:menril_log_filled")
    public static final Block BLOCK_MENRIL_LOG_FILLED = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:menril_log_stripped")
    public static final Block BLOCK_MENRIL_LOG_STRIPPED = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:menril_wood")
    public static final Block BLOCK_MENRIL_WOOD = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:menril_wood_stripped")
    public static final Block BLOCK_MENRIL_WOOD_STRIPPED = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:menril_planks")
    public static final Block BLOCK_MENRIL_PLANKS = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:menril_planks_stairs")
    public static final Block BLOCK_MENRIL_PLANKS_STAIRS = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:menril_sapling")
    public static final Block BLOCK_MENRIL_SAPLING = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:proxy")
    public static final Block BLOCK_PROXY = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:squeezer")
    public static final BlockSqueezer BLOCK_SQUEEZER = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:variablestore")
    public static final Block BLOCK_VARIABLE_STORE = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:menril_torch")
    public static final Block BLOCK_MENRIL_TORCH = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:menril_torch_wall")
    public static final Block BLOCK_MENRIL_TORCH_WALL = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:menril_torch_stone")
    public static final Block BLOCK_MENRIL_TORCH_STONE = null;
    @ObjectHolder(registryName = "block", value = "integrateddynamics:menril_torch_stone_wall")
    public static final Block BLOCK_MENRIL_TORCH_STONE_WALL = null;

    @ObjectHolder(registryName = "block_entity_type", value = "integrateddynamics:coal_generator")
    public static final BlockEntityType<BlockEntityCoalGenerator> BLOCK_ENTITY_COAL_GENERATOR = null;
    @ObjectHolder(registryName = "block_entity_type", value = "integrateddynamics:delay")
    public static final BlockEntityType<BlockEntityDelay> BLOCK_ENTITY_DELAY = null;
    @ObjectHolder(registryName = "block_entity_type", value = "integrateddynamics:drying_basin")
    public static final BlockEntityType<BlockEntityDryingBasin> BLOCK_ENTITY_DRYING_BASIN = null;
    @ObjectHolder(registryName = "block_entity_type", value = "integrateddynamics:energy_battery")
    public static final BlockEntityType<BlockEntityEnergyBattery> BLOCK_ENTITY_ENERGY_BATTERY = null;
    @ObjectHolder(registryName = "block_entity_type", value = "integrateddynamics:materializer")
    public static final BlockEntityType<BlockEntityMaterializer> BLOCK_ENTITY_MATERIALIZER = null;
    @ObjectHolder(registryName = "block_entity_type", value = "integrateddynamics:mechanical_drying_basin")
    public static final BlockEntityType<BlockEntityMechanicalDryingBasin> BLOCK_ENTITY_MECHANICAL_DRYING_BASIN = null;
    @ObjectHolder(registryName = "block_entity_type", value = "integrateddynamics:mechanical_squeezer")
    public static final BlockEntityType<BlockEntityMechanicalSqueezer> BLOCK_ENTITY_MECHANICAL_SQUEEZER = null;
    @ObjectHolder(registryName = "block_entity_type", value = "integrateddynamics:multipart_ticking")
    public static final BlockEntityType<BlockEntityMultipartTicking> BLOCK_ENTITY_MULTIPART_TICKING = null;
    @ObjectHolder(registryName = "block_entity_type", value = "integrateddynamics:proxy")
    public static final BlockEntityType<BlockEntityProxy> BLOCK_ENTITY_PROXY = null;
    @ObjectHolder(registryName = "block_entity_type", value = "integrateddynamics:squeezer")
    public static final BlockEntityType<BlockEntitySqueezer> BLOCK_ENTITY_SQUEEZER = null;
    @ObjectHolder(registryName = "block_entity_type", value = "integrateddynamics:variable_store")
    public static final BlockEntityType<BlockEntityVariablestore> BLOCK_ENTITY_VARIABLE_STORE = null;

    @ObjectHolder(registryName = "menu", value = "integrateddynamics:aspect_settings")
    public static final MenuType<ContainerAspectSettings> CONTAINER_ASPECT_SETTINGS = null;
    @ObjectHolder(registryName = "menu", value = "integrateddynamics:coal_generator")
    public static final MenuType<ContainerCoalGenerator> CONTAINER_COAL_GENERATOR = null;
    @ObjectHolder(registryName = "menu", value = "integrateddynamics:delay")
    public static final MenuType<ContainerDelay> CONTAINER_DELAY = null;
    @ObjectHolder(registryName = "menu", value = "integrateddynamics:labeller")
    public static final MenuType<ContainerLabeller> CONTAINER_LABELLER = null;
    @ObjectHolder(registryName = "menu", value = "integrateddynamics:logic_programmer")
    public static final MenuType<ContainerLogicProgrammer> CONTAINER_LOGIC_PROGRAMMER = null;
    @ObjectHolder(registryName = "menu", value = "integrateddynamics:logic_programmer_portable")
    public static final MenuType<ContainerLogicProgrammerPortable> CONTAINER_LOGIC_PROGRAMMER_PORTABLE = null;
    @ObjectHolder(registryName = "menu", value = "integrateddynamics:materializer")
    public static final MenuType<ContainerMaterializer> CONTAINER_MATERIALIZER = null;
    @ObjectHolder(registryName = "menu", value = "integrateddynamics:mechanical_drying_basin")
    public static final MenuType<ContainerMaterializer> CONTAINER_MECHANICAL_DRYING_BASIN = null;
    @ObjectHolder(registryName = "menu", value = "integrateddynamics:mechanical_squeezer")
    public static final MenuType<ContainerMaterializer> CONTAINER_MECHANICAL_SQUEEZER = null;
    @ObjectHolder(registryName = "menu", value = "integrateddynamics:on_the_dynamics_of_integration")
    public static final MenuType<ContainerOnTheDynamicsOfIntegration> CONTAINER_ON_THE_DYNAMICS_OF_INTEGRATION = null;
    @ObjectHolder(registryName = "menu", value = "integrateddynamics:part_display")
    public static final MenuType<ContainerPartPanelVariableDriven> CONTAINER_PART_DISPLAY = null;
    @ObjectHolder(registryName = "menu", value = "integrateddynamics:part_reader")
    public static final MenuType<ContainerPartReader> CONTAINER_PART_READER = null;
    @ObjectHolder(registryName = "menu", value = "integrateddynamics:part_settings")
    public static final MenuType<ContainerPartSettings> CONTAINER_PART_SETTINGS = null;
    @ObjectHolder(registryName = "menu", value = "integrateddynamics:part_writer")
    public static final MenuType<ContainerPartReader> CONTAINER_PART_WRITER = null;
    @ObjectHolder(registryName = "menu", value = "integrateddynamics:proxy")
    public static final MenuType<ContainerProxy> CONTAINER_PROXY = null;
    @ObjectHolder(registryName = "menu", value = "integrateddynamics:variablestore")
    public static final MenuType<ContainerVariablestore> CONTAINER_VARIABLESTORE = null;

    @ObjectHolder(registryName = "fluid", value = "integrateddynamics:liquid_chorus")
    public static final FlowingFluid FLUID_LIQUID_CHORUS = null;
    @ObjectHolder(registryName = "fluid", value = "integrateddynamics:menril_resin")
    public static final FlowingFluid FLUID_MENRIL_RESIN = null;

    @ObjectHolder(registryName = "worldgen/foliage_placer_type", value = "integrateddynamics:menril")
    public static final FoliagePlacerType<FoliagePlacerMenril> FOLIAGE_PLACER_MENRIL = null;
    public static TrunkPlacerType<TrunkPlacerMenril> TRUNK_PLACER_MENRIL = null; // Trunk placer types are not Forge registries unfortunately...
    @ObjectHolder(registryName = "worldgen/biome", value = "integrateddynamics:meneglin")
    public static final Biome BIOME_MENEGLIN = null;

    @ObjectHolder(registryName = "recipe_type", value = "integrateddynamics:drying_basin")
    public static final RecipeType<RecipeDryingBasin> RECIPETYPE_DRYING_BASIN = null;
    @ObjectHolder(registryName = "recipe_type", value = "integrateddynamics:mechanical_drying_basin")
    public static final RecipeType<RecipeMechanicalDryingBasin> RECIPETYPE_MECHANICAL_DRYING_BASIN = null;
    @ObjectHolder(registryName = "recipe_type", value = "integrateddynamics:squeezer")
    public static final RecipeType<RecipeSqueezer> RECIPETYPE_SQUEEZER = null;
    @ObjectHolder(registryName = "recipe_type", value = "integrateddynamics:mechanical_squeezer")
    public static final RecipeType<RecipeMechanicalSqueezer> RECIPETYPE_MECHANICAL_SQUEEZER = null;

    @ObjectHolder(registryName = "recipe_serializer", value = "integrateddynamics:drying_basin")
    public static final RecipeSerializer<RecipeDryingBasin> RECIPESERIALIZER_DRYING_BASIN = null;
    @ObjectHolder(registryName = "recipe_serializer", value = "integrateddynamics:mechanical_drying_basin")
    public static final RecipeSerializer<RecipeMechanicalDryingBasin> RECIPESERIALIZER_MECHANICAL_DRYING_BASIN = null;
    @ObjectHolder(registryName = "recipe_serializer", value = "integrateddynamics:squeezer")
    public static final RecipeSerializer<RecipeSqueezer> RECIPESERIALIZER_SQUEEZER = null;
    @ObjectHolder(registryName = "recipe_serializer", value = "integrateddynamics:mechanical_squeezer")
    public static final RecipeSerializer<RecipeMechanicalSqueezer> RECIPESERIALIZER_MECHANICAL_SQUEEZER = null;
    @ObjectHolder(registryName = "recipe_serializer", value = "integrateddynamics:crafting_special_nbt_clear")
    public static final RecipeSerializer<RecipeNbtClear> RECIPESERIALIZER_NBT_CLEAR = null;
    @ObjectHolder(registryName = "recipe_serializer", value = "integrateddynamics:crafting_special_energycontainer_combination")
    public static final RecipeSerializer<RecipeEnergyContainerCombination> RECIPESERIALIZER_ENERGY_CONTAINER_COMBINATION = null;
    @ObjectHolder(registryName = "recipe_serializer", value = "integrateddynamics:crafting_special_facade")
    public static final SimpleCraftingRecipeSerializer<ItemFacadeRecipe> RECIPESERIALIZER_FACADE = null;
    @ObjectHolder(registryName = "recipe_serializer", value = "integrateddynamics:crafting_special_variable_copy")
    public static final SimpleCraftingRecipeSerializer<ItemVariableCopyRecipe> RECIPESERIALIZER_VARIABLE_COPY = null;


}
