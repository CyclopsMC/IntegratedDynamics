package org.cyclops.integrateddynamics;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
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

    @ObjectHolder("integrateddynamics:bucket_liquid_chorus")
    public static final Item ITEM_BUCKET_LIQUID_CHORUS = null;
    @ObjectHolder("integrateddynamics:bucket_menril_resin")
    public static final Item ITEM_BUCKET_MENRIL_RESIN = null;
    @ObjectHolder("integrateddynamics:cable")
    public static final Item ITEM_CABLE = null;
    @ObjectHolder("integrateddynamics:delay")
    public static final Item ITEM_DELAY = null;
    @ObjectHolder("integrateddynamics:energy_battery")
    public static final ItemBlockEnergyContainer ITEM_ENERGY_BATTERY = null;
    @ObjectHolder("integrateddynamics:facade")
    public static final ItemFacade ITEM_FACADE = null;
    @ObjectHolder("integrateddynamics:on_the_dynamics_of_integration")
    public static final Item ITEM_ON_THE_DYNAMICS_OF_INTEGRATION = null;
    @ObjectHolder("integrateddynamics:labeller")
    public static final Item ITEM_LABELLER = null;
    @ObjectHolder("integrateddynamics:portable_logic_programmer")
    public static final Item ITEM_PORTABLE_LOGIC_PROGRAMMER = null;
    @ObjectHolder("integrateddynamics:proxy")
    public static final Item ITEM_PROXY = null;
    @ObjectHolder("integrateddynamics:variable")
    public static final ItemVariable ITEM_VARIABLE = null;
    @ObjectHolder("integrateddynamics:wrench")
    public static final Item ITEM_WRENCH = null;
    @ObjectHolder("integrateddynamics:menril_torch")
    public static final Item ITEM_MENRIL_TORCH = null;
    @ObjectHolder("integrateddynamics:menril_torch_stone")
    public static final Item ITEM_MENRIL_TORCH_STONE = null;

    @ObjectHolder("integrateddynamics:cable")
    public static final BlockCable BLOCK_CABLE = null;
    @ObjectHolder("integrateddynamics:coal_generator")
    public static final Block BLOCK_COAL_GENERATOR = null;
    @ObjectHolder("integrateddynamics:crystalized_menril_brick")
    public static final Block BLOCK_CRYSTALIZED_MENRIL_BRICK = null;
    @ObjectHolder("integrateddynamics:crystalized_menril_block")
    public static final Block BLOCK_CRYSTALIZED_MENRIL_BLOCK = null;
    @ObjectHolder("integrateddynamics:crystalized_chorus_brick")
    public static final Block BLOCK_CRYSTALIZED_CHORUS_BRICK = null;
    @ObjectHolder("integrateddynamics:crystalized_chorus_block")
    public static final Block BLOCK_CRYSTALIZED_CHORUS_BLOCK = null;
    @ObjectHolder("integrateddynamics:delay")
    public static final Block BLOCK_DELAY = null;
    @ObjectHolder("integrateddynamics:drying_basin")
    public static final BlockDryingBasin BLOCK_DRYING_BASIN = null;
    @ObjectHolder("integrateddynamics:energy_battery")
    public static final BlockEnergyBattery BLOCK_ENERGY_BATTERY = null;
    @ObjectHolder("integrateddynamics:block_liquid_chorus")
    public static final BlockFluidLiquidChorus BLOCK_FLUID_LIQUID_CHORUS = null;
    @ObjectHolder("integrateddynamics:block_menril_resin")
    public static final BlockFluidMenrilResin BLOCK_FLUID_MENRIL_RESIN = null;
    @ObjectHolder("integrateddynamics:invisible_light")
    public static final Block BLOCK_INVISIBLE_LIGHT = null;
    @ObjectHolder("integrateddynamics:logic_programmer")
    public static final Block BLOCK_LOGIC_PROGRAMMER = null;
    @ObjectHolder("integrateddynamics:materializer")
    public static final Block BLOCK_MATERIALIZER = null;
    @ObjectHolder("integrateddynamics:mechanical_drying_basin")
    public static final BlockMechanicalDryingBasin BLOCK_MECHANICAL_DRYING_BASIN = null;
    @ObjectHolder("integrateddynamics:mechanical_squeezer")
    public static final BlockMechanicalSqueezer BLOCK_MECHANICAL_SQUEEZER = null;
    @ObjectHolder("integrateddynamics:menril_leaves")
    public static final Block BLOCK_MENRIL_LEAVES = null;
    @ObjectHolder("integrateddynamics:menril_log")
    public static final Block BLOCK_MENRIL_LOG = null;
    @ObjectHolder("integrateddynamics:menril_log_filled")
    public static final Block BLOCK_MENRIL_LOG_FILLED = null;
    @ObjectHolder("integrateddynamics:menril_log_stripped")
    public static final Block BLOCK_MENRIL_LOG_STRIPPED = null;
    @ObjectHolder("integrateddynamics:menril_wood")
    public static final Block BLOCK_MENRIL_WOOD = null;
    @ObjectHolder("integrateddynamics:menril_wood_stripped")
    public static final Block BLOCK_MENRIL_WOOD_STRIPPED = null;
    @ObjectHolder("integrateddynamics:menril_planks")
    public static final Block BLOCK_MENRIL_PLANKS = null;
    @ObjectHolder("integrateddynamics:menril_planks_stairs")
    public static final Block BLOCK_MENRIL_PLANKS_STAIRS = null;
    @ObjectHolder("integrateddynamics:menril_sapling")
    public static final Block BLOCK_MENRIL_SAPLING = null;
    @ObjectHolder("integrateddynamics:proxy")
    public static final Block BLOCK_PROXY = null;
    @ObjectHolder("integrateddynamics:squeezer")
    public static final BlockSqueezer BLOCK_SQUEEZER = null;
    @ObjectHolder("integrateddynamics:variablestore")
    public static final Block BLOCK_VARIABLE_STORE = null;
    @ObjectHolder("integrateddynamics:menril_torch")
    public static final Block BLOCK_MENRIL_TORCH = null;
    @ObjectHolder("integrateddynamics:menril_torch_wall")
    public static final Block BLOCK_MENRIL_TORCH_WALL = null;
    @ObjectHolder("integrateddynamics:menril_torch_stone")
    public static final Block BLOCK_MENRIL_TORCH_STONE = null;
    @ObjectHolder("integrateddynamics:menril_torch_stone_wall")
    public static final Block BLOCK_MENRIL_TORCH_STONE_WALL = null;

    @ObjectHolder("integrateddynamics:coal_generator")
    public static final BlockEntityType<BlockEntityCoalGenerator> BLOCK_ENTITY_COAL_GENERATOR = null;
    @ObjectHolder("integrateddynamics:delay")
    public static final BlockEntityType<BlockEntityDelay> BLOCK_ENTITY_DELAY = null;
    @ObjectHolder("integrateddynamics:drying_basin")
    public static final BlockEntityType<BlockEntityDryingBasin> BLOCK_ENTITY_DRYING_BASIN = null;
    @ObjectHolder("integrateddynamics:energy_battery")
    public static final BlockEntityType<BlockEntityEnergyBattery> BLOCK_ENTITY_ENERGY_BATTERY = null;
    @ObjectHolder("integrateddynamics:materializer")
    public static final BlockEntityType<BlockEntityMaterializer> BLOCK_ENTITY_MATERIALIZER = null;
    @ObjectHolder("integrateddynamics:mechanical_drying_basin")
    public static final BlockEntityType<BlockEntityMechanicalDryingBasin> BLOCK_ENTITY_MECHANICAL_DRYING_BASIN = null;
    @ObjectHolder("integrateddynamics:mechanical_squeezer")
    public static final BlockEntityType<BlockEntityMechanicalSqueezer> BLOCK_ENTITY_MECHANICAL_SQUEEZER = null;
    @ObjectHolder("integrateddynamics:multipart_ticking")
    public static final BlockEntityType<BlockEntityMultipartTicking> BLOCK_ENTITY_MULTIPART_TICKING = null;
    @ObjectHolder("integrateddynamics:proxy")
    public static final BlockEntityType<BlockEntityProxy> BLOCK_ENTITY_PROXY = null;
    @ObjectHolder("integrateddynamics:squeezer")
    public static final BlockEntityType<BlockEntitySqueezer> BLOCK_ENTITY_SQUEEZER = null;
    @ObjectHolder("integrateddynamics:variable_store")
    public static final BlockEntityType<BlockEntityVariablestore> BLOCK_ENTITY_VARIABLE_STORE = null;

    @ObjectHolder("integrateddynamics:aspect_settings")
    public static final MenuType<ContainerAspectSettings> CONTAINER_ASPECT_SETTINGS = null;
    @ObjectHolder("integrateddynamics:coal_generator")
    public static final MenuType<ContainerCoalGenerator> CONTAINER_COAL_GENERATOR = null;
    @ObjectHolder("integrateddynamics:delay")
    public static final MenuType<ContainerDelay> CONTAINER_DELAY = null;
    @ObjectHolder("integrateddynamics:labeller")
    public static final MenuType<ContainerLabeller> CONTAINER_LABELLER = null;
    @ObjectHolder("integrateddynamics:logic_programmer")
    public static final MenuType<ContainerLogicProgrammer> CONTAINER_LOGIC_PROGRAMMER = null;
    @ObjectHolder("integrateddynamics:logic_programmer_portable")
    public static final MenuType<ContainerLogicProgrammerPortable> CONTAINER_LOGIC_PROGRAMMER_PORTABLE = null;
    @ObjectHolder("integrateddynamics:materializer")
    public static final MenuType<ContainerMaterializer> CONTAINER_MATERIALIZER = null;
    @ObjectHolder("integrateddynamics:mechanical_drying_basin")
    public static final MenuType<ContainerMaterializer> CONTAINER_MECHANICAL_DRYING_BASIN = null;
    @ObjectHolder("integrateddynamics:mechanical_squeezer")
    public static final MenuType<ContainerMaterializer> CONTAINER_MECHANICAL_SQUEEZER = null;
    @ObjectHolder("integrateddynamics:on_the_dynamics_of_integration")
    public static final MenuType<ContainerOnTheDynamicsOfIntegration> CONTAINER_ON_THE_DYNAMICS_OF_INTEGRATION = null;
    @ObjectHolder("integrateddynamics:part_display")
    public static final MenuType<ContainerPartPanelVariableDriven> CONTAINER_PART_DISPLAY = null;
    @ObjectHolder("integrateddynamics:part_reader")
    public static final MenuType<ContainerPartReader> CONTAINER_PART_READER = null;
    @ObjectHolder("integrateddynamics:part_settings")
    public static final MenuType<ContainerPartSettings> CONTAINER_PART_SETTINGS = null;
    @ObjectHolder("integrateddynamics:part_writer")
    public static final MenuType<ContainerPartReader> CONTAINER_PART_WRITER = null;
    @ObjectHolder("integrateddynamics:proxy")
    public static final MenuType<ContainerProxy> CONTAINER_PROXY = null;
    @ObjectHolder("integrateddynamics:variablestore")
    public static final MenuType<ContainerVariablestore> CONTAINER_VARIABLESTORE = null;

    @ObjectHolder("integrateddynamics:liquid_chorus")
    public static final FlowingFluid FLUID_LIQUID_CHORUS = null;
    @ObjectHolder("integrateddynamics:menril_resin")
    public static final FlowingFluid FLUID_MENRIL_RESIN = null;

    @ObjectHolder("integrateddynamics:menril")
    public static final FoliagePlacerType<FoliagePlacerMenril> FOLIAGE_PLACER_MENRIL = null;
    public static TrunkPlacerType<TrunkPlacerMenril> TRUNK_PLACER_MENRIL = null; // Trunk placer types are not Forge registries unfortunately...
    @ObjectHolder("integrateddynamics:meneglin")
    public static final Biome BIOME_MENEGLIN = null;

    // Recipe types are not Forge registries unfortunately...
    public static RecipeType<RecipeDryingBasin> RECIPETYPE_DRYING_BASIN = null;
    public static RecipeType<RecipeMechanicalDryingBasin> RECIPETYPE_MECHANICAL_DRYING_BASIN = null;
    public static RecipeType<RecipeSqueezer> RECIPETYPE_SQUEEZER = null;
    public static RecipeType<RecipeMechanicalSqueezer> RECIPETYPE_MECHANICAL_SQUEEZER = null;

    @ObjectHolder("integrateddynamics:drying_basin")
    public static final RecipeSerializer<RecipeDryingBasin> RECIPESERIALIZER_DRYING_BASIN = null;
    @ObjectHolder("integrateddynamics:mechanical_drying_basin")
    public static final RecipeSerializer<RecipeMechanicalDryingBasin> RECIPESERIALIZER_MECHANICAL_DRYING_BASIN = null;
    @ObjectHolder("integrateddynamics:squeezer")
    public static final RecipeSerializer<RecipeSqueezer> RECIPESERIALIZER_SQUEEZER = null;
    @ObjectHolder("integrateddynamics:mechanical_squeezer")
    public static final RecipeSerializer<RecipeMechanicalSqueezer> RECIPESERIALIZER_MECHANICAL_SQUEEZER = null;
    @ObjectHolder("integrateddynamics:crafting_special_nbt_clear")
    public static final RecipeSerializer<RecipeNbtClear> RECIPESERIALIZER_NBT_CLEAR = null;
    @ObjectHolder("integrateddynamics:crafting_special_energycontainer_combination")
    public static final RecipeSerializer<RecipeEnergyContainerCombination> RECIPESERIALIZER_ENERGY_CONTAINER_COMBINATION = null;
    @ObjectHolder("integrateddynamics:crafting_special_facade")
    public static final SimpleRecipeSerializer<ItemFacadeRecipe> RECIPESERIALIZER_FACADE = null;
    @ObjectHolder("integrateddynamics:crafting_special_variable_copy")
    public static final SimpleRecipeSerializer<ItemVariableCopyRecipe> RECIPESERIALIZER_VARIABLE_COPY = null;


}
