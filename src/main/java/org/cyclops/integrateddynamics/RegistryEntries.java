package org.cyclops.integrateddynamics;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.apache.commons.lang3.tuple.Pair;
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
import org.cyclops.integrateddynamics.entity.item.EntityItemTargetted;
import org.cyclops.integrateddynamics.inventory.container.*;
import org.cyclops.integrateddynamics.item.ItemEnhancement;
import org.cyclops.integrateddynamics.item.ItemFacade;
import org.cyclops.integrateddynamics.item.ItemVariable;
import org.cyclops.integrateddynamics.item.ItemWrench;
import org.cyclops.integrateddynamics.recipe.ItemFacadeRecipe;
import org.cyclops.integrateddynamics.recipe.ItemVariableCopyRecipe;
import org.cyclops.integrateddynamics.world.gen.foliageplacer.FoliagePlacerMenril;
import org.cyclops.integrateddynamics.world.gen.trunkplacer.TrunkPlacerMenril;

/**
 * Referenced registry entries.
 * @author rubensworks
 */
public class RegistryEntries {

    public static final DeferredHolder<Item, Item> ITEM_BUCKET_LIQUID_CHORUS = DeferredHolder.create(Registries.ITEM, ResourceLocation.parse("integrateddynamics:bucket_liquid_chorus"));
    public static final DeferredHolder<Item, Item> ITEM_BUCKET_MENRIL_RESIN = DeferredHolder.create(Registries.ITEM, ResourceLocation.parse("integrateddynamics:bucket_menril_resin"));
    public static final DeferredHolder<Item, Item> ITEM_CABLE = DeferredHolder.create(Registries.ITEM, ResourceLocation.parse("integrateddynamics:cable"));
    public static final DeferredHolder<Item, Item> ITEM_DELAY = DeferredHolder.create(Registries.ITEM, ResourceLocation.parse("integrateddynamics:delay"));
    public static final DeferredHolder<Item, ItemBlockEnergyContainer> ITEM_ENERGY_BATTERY = DeferredHolder.create(Registries.ITEM, ResourceLocation.parse("integrateddynamics:energy_battery"));
    public static final DeferredHolder<Item, ItemFacade> ITEM_FACADE = DeferredHolder.create(Registries.ITEM, ResourceLocation.parse("integrateddynamics:facade"));
    public static final DeferredHolder<Item, Item> ITEM_ON_THE_DYNAMICS_OF_INTEGRATION = DeferredHolder.create(Registries.ITEM, ResourceLocation.parse("integrateddynamics:on_the_dynamics_of_integration"));
    public static final DeferredHolder<Item, Item> ITEM_LABELLER = DeferredHolder.create(Registries.ITEM, ResourceLocation.parse("integrateddynamics:labeller"));
    public static final DeferredHolder<Item, Item> ITEM_PORTABLE_LOGIC_PROGRAMMER = DeferredHolder.create(Registries.ITEM, ResourceLocation.parse("integrateddynamics:portable_logic_programmer"));
    public static final DeferredHolder<Item, Item> ITEM_PROXY = DeferredHolder.create(Registries.ITEM, ResourceLocation.parse("integrateddynamics:proxy"));
    public static final DeferredHolder<Item, ItemVariable> ITEM_VARIABLE = DeferredHolder.create(Registries.ITEM, ResourceLocation.parse("integrateddynamics:variable"));
    public static final DeferredHolder<Item, Item> ITEM_WRENCH = DeferredHolder.create(Registries.ITEM, ResourceLocation.parse("integrateddynamics:wrench"));
    public static final DeferredHolder<Item, Item> ITEM_MENRIL_TORCH = DeferredHolder.create(Registries.ITEM, ResourceLocation.parse("integrateddynamics:menril_torch"));
    public static final DeferredHolder<Item, Item> ITEM_MENRIL_TORCH_STONE = DeferredHolder.create(Registries.ITEM, ResourceLocation.parse("integrateddynamics:menril_torch_stone"));
    public static final DeferredHolder<Item, ItemEnhancement> ITEM_ENHANCEMENT_OFFSET = DeferredHolder.create(Registries.ITEM, ResourceLocation.parse("integrateddynamics:enhancement_offset"));

    public static final DeferredHolder<Block, BlockCable> BLOCK_CABLE = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:cable"));
    public static final DeferredHolder<Block, Block> BLOCK_COAL_GENERATOR = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:coal_generator"));
    public static final DeferredHolder<Block, Block> BLOCK_CRYSTALIZED_MENRIL_BRICK = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:crystalized_menril_brick"));
    public static final DeferredHolder<Block, Block> BLOCK_CRYSTALIZED_MENRIL_BLOCK = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:crystalized_menril_block"));
    public static final DeferredHolder<Block, Block> BLOCK_CRYSTALIZED_CHORUS_BRICK = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:crystalized_chorus_brick"));
    public static final DeferredHolder<Block, Block> BLOCK_CRYSTALIZED_CHORUS_BLOCK = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:crystalized_chorus_block"));
    public static final DeferredHolder<Block, Block> BLOCK_DELAY = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:delay"));
    public static final DeferredHolder<Block, BlockDryingBasin> BLOCK_DRYING_BASIN = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:drying_basin"));
    public static final DeferredHolder<Block, BlockEnergyBattery> BLOCK_ENERGY_BATTERY = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:energy_battery"));
    public static final DeferredHolder<Block, BlockFluidLiquidChorus> BLOCK_FLUID_LIQUID_CHORUS = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:block_liquid_chorus"));
    public static final DeferredHolder<Block, BlockFluidMenrilResin> BLOCK_FLUID_MENRIL_RESIN = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:block_menril_resin"));
    public static final DeferredHolder<Block, Block> BLOCK_INVISIBLE_LIGHT = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:invisible_light"));
    public static final DeferredHolder<Block, Block> BLOCK_LOGIC_PROGRAMMER = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:logic_programmer"));
    public static final DeferredHolder<Block, Block> BLOCK_MATERIALIZER = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:materializer"));
    public static final DeferredHolder<Block, BlockMechanicalDryingBasin> BLOCK_MECHANICAL_DRYING_BASIN = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:mechanical_drying_basin"));
    public static final DeferredHolder<Block, BlockMechanicalSqueezer> BLOCK_MECHANICAL_SQUEEZER = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:mechanical_squeezer"));
    public static final DeferredHolder<Block, Block> BLOCK_MENRIL_LEAVES = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:menril_leaves"));
    public static final DeferredHolder<Block, Block> BLOCK_MENRIL_LOG = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:menril_log"));
    public static final DeferredHolder<Block, Block> BLOCK_MENRIL_LOG_FILLED = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:menril_log_filled"));
    public static final DeferredHolder<Block, Block> BLOCK_MENRIL_LOG_STRIPPED = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:menril_log_stripped"));
    public static final DeferredHolder<Block, Block> BLOCK_MENRIL_WOOD = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:menril_wood"));
    public static final DeferredHolder<Block, Block> BLOCK_MENRIL_WOOD_STRIPPED = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:menril_wood_stripped"));
    public static final DeferredHolder<Block, Block> BLOCK_MENRIL_PLANKS = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:menril_planks"));
    public static final DeferredHolder<Block, Block> BLOCK_MENRIL_PLANKS_STAIRS = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:menril_planks_stairs"));
    public static final DeferredHolder<Block, Block> BLOCK_MENRIL_SAPLING = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:menril_sapling"));
    public static final DeferredHolder<Block, Block> BLOCK_PROXY = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:proxy"));
    public static final DeferredHolder<Block, BlockSqueezer> BLOCK_SQUEEZER = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:squeezer"));
    public static final DeferredHolder<Block, Block> BLOCK_VARIABLE_STORE = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:variablestore"));
    public static final DeferredHolder<Block, Block> BLOCK_MENRIL_TORCH = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:menril_torch"));
    public static final DeferredHolder<Block, Block> BLOCK_MENRIL_TORCH_WALL = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:menril_torch_wall"));
    public static final DeferredHolder<Block, Block> BLOCK_MENRIL_TORCH_STONE = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:menril_torch_stone"));
    public static final DeferredHolder<Block, Block> BLOCK_MENRIL_TORCH_STONE_WALL = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integrateddynamics:menril_torch_stone_wall"));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityCoalGenerator>> BLOCK_ENTITY_COAL_GENERATOR = DeferredHolder.create(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.parse("integrateddynamics:coal_generator"));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityDelay>> BLOCK_ENTITY_DELAY = DeferredHolder.create(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.parse("integrateddynamics:delay"));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityDryingBasin>> BLOCK_ENTITY_DRYING_BASIN = DeferredHolder.create(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.parse("integrateddynamics:drying_basin"));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityEnergyBattery>> BLOCK_ENTITY_ENERGY_BATTERY = DeferredHolder.create(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.parse("integrateddynamics:energy_battery"));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityMaterializer>> BLOCK_ENTITY_MATERIALIZER = DeferredHolder.create(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.parse("integrateddynamics:materializer"));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityMechanicalDryingBasin>> BLOCK_ENTITY_MECHANICAL_DRYING_BASIN = DeferredHolder.create(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.parse("integrateddynamics:mechanical_drying_basin"));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityMechanicalSqueezer>> BLOCK_ENTITY_MECHANICAL_SQUEEZER = DeferredHolder.create(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.parse("integrateddynamics:mechanical_squeezer"));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityMultipartTicking>> BLOCK_ENTITY_MULTIPART_TICKING = DeferredHolder.create(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.parse("integrateddynamics:multipart_ticking"));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityProxy>> BLOCK_ENTITY_PROXY = DeferredHolder.create(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.parse("integrateddynamics:proxy"));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntitySqueezer>> BLOCK_ENTITY_SQUEEZER = DeferredHolder.create(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.parse("integrateddynamics:squeezer"));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityVariablestore>> BLOCK_ENTITY_VARIABLE_STORE = DeferredHolder.create(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.parse("integrateddynamics:variable_store"));

    public static final DeferredHolder<MenuType<?>, MenuType<ContainerAspectSettings>> CONTAINER_ASPECT_SETTINGS = DeferredHolder.create(Registries.MENU, ResourceLocation.parse("integrateddynamics:aspect_settings"));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerCoalGenerator>> CONTAINER_COAL_GENERATOR = DeferredHolder.create(Registries.MENU, ResourceLocation.parse("integrateddynamics:coal_generator"));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerDelay>> CONTAINER_DELAY = DeferredHolder.create(Registries.MENU, ResourceLocation.parse("integrateddynamics:delay"));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerLabeller>> CONTAINER_LABELLER = DeferredHolder.create(Registries.MENU, ResourceLocation.parse("integrateddynamics:labeller"));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerLogicProgrammer>> CONTAINER_LOGIC_PROGRAMMER = DeferredHolder.create(Registries.MENU, ResourceLocation.parse("integrateddynamics:logic_programmer"));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerLogicProgrammerPortable>> CONTAINER_LOGIC_PROGRAMMER_PORTABLE = DeferredHolder.create(Registries.MENU, ResourceLocation.parse("integrateddynamics:logic_programmer_portable"));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerMaterializer>> CONTAINER_MATERIALIZER = DeferredHolder.create(Registries.MENU, ResourceLocation.parse("integrateddynamics:materializer"));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerMaterializer>> CONTAINER_MECHANICAL_DRYING_BASIN = DeferredHolder.create(Registries.MENU, ResourceLocation.parse("integrateddynamics:mechanical_drying_basin"));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerMaterializer>> CONTAINER_MECHANICAL_SQUEEZER = DeferredHolder.create(Registries.MENU, ResourceLocation.parse("integrateddynamics:mechanical_squeezer"));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerOnTheDynamicsOfIntegration>> CONTAINER_ON_THE_DYNAMICS_OF_INTEGRATION = DeferredHolder.create(Registries.MENU, ResourceLocation.parse("integrateddynamics:on_the_dynamics_of_integration"));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerPartPanelVariableDriven>> CONTAINER_PART_DISPLAY = DeferredHolder.create(Registries.MENU, ResourceLocation.parse("integrateddynamics:part_display"));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerPartReader>> CONTAINER_PART_READER = DeferredHolder.create(Registries.MENU, ResourceLocation.parse("integrateddynamics:part_reader"));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerPartSettings>> CONTAINER_PART_SETTINGS = DeferredHolder.create(Registries.MENU, ResourceLocation.parse("integrateddynamics:part_settings"));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerPartSettings>> CONTAINER_PART_OFFSET = DeferredHolder.create(Registries.MENU, ResourceLocation.parse("integrateddynamics:part_offset"));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerPartReader>> CONTAINER_PART_WRITER = DeferredHolder.create(Registries.MENU, ResourceLocation.parse("integrateddynamics:part_writer"));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerProxy>> CONTAINER_PROXY = DeferredHolder.create(Registries.MENU, ResourceLocation.parse("integrateddynamics:proxy"));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerVariablestore>> CONTAINER_VARIABLESTORE = DeferredHolder.create(Registries.MENU, ResourceLocation.parse("integrateddynamics:variablestore"));

    public static final DeferredHolder<Fluid, FlowingFluid> FLUID_LIQUID_CHORUS = DeferredHolder.create(Registries.FLUID, ResourceLocation.parse("integrateddynamics:liquid_chorus"));
    public static final DeferredHolder<Fluid, FlowingFluid> FLUID_MENRIL_RESIN = DeferredHolder.create(Registries.FLUID, ResourceLocation.parse("integrateddynamics:menril_resin"));

    public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<FoliagePlacerMenril>> FOLIAGE_PLACER_MENRIL = DeferredHolder.create(Registries.FOLIAGE_PLACER_TYPE, ResourceLocation.parse("integrateddynamics:menril"));

    public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<TrunkPlacerMenril>> TRUNK_PLACER_MENRIL = DeferredHolder.create(Registries.TRUNK_PLACER_TYPE, ResourceLocation.parse("integrateddynamics:menril"));

    public static final DeferredHolder<Biome, Biome> BIOME_MENEGLIN = DeferredHolder.create(Registries.BIOME, ResourceLocation.parse("integrateddynamics:meneglin"));

    public static final DeferredHolder<RecipeType<?>, RecipeType<RecipeDryingBasin>> RECIPETYPE_DRYING_BASIN = DeferredHolder.create(Registries.RECIPE_TYPE, ResourceLocation.parse("integrateddynamics:drying_basin"));
    public static final DeferredHolder<RecipeType<?>, RecipeType<RecipeMechanicalDryingBasin>> RECIPETYPE_MECHANICAL_DRYING_BASIN = DeferredHolder.create(Registries.RECIPE_TYPE, ResourceLocation.parse("integrateddynamics:mechanical_drying_basin"));
    public static final DeferredHolder<RecipeType<?>, RecipeType<RecipeSqueezer>> RECIPETYPE_SQUEEZER = DeferredHolder.create(Registries.RECIPE_TYPE, ResourceLocation.parse("integrateddynamics:squeezer"));
    public static final DeferredHolder<RecipeType<?>, RecipeType<RecipeMechanicalSqueezer>> RECIPETYPE_MECHANICAL_SQUEEZER = DeferredHolder.create(Registries.RECIPE_TYPE, ResourceLocation.parse("integrateddynamics:mechanical_squeezer"));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RecipeDryingBasin>> RECIPESERIALIZER_DRYING_BASIN = DeferredHolder.create(Registries.RECIPE_SERIALIZER, ResourceLocation.parse("integrateddynamics:drying_basin"));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RecipeMechanicalDryingBasin>> RECIPESERIALIZER_MECHANICAL_DRYING_BASIN = DeferredHolder.create(Registries.RECIPE_SERIALIZER, ResourceLocation.parse("integrateddynamics:mechanical_drying_basin"));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RecipeSqueezer>> RECIPESERIALIZER_SQUEEZER = DeferredHolder.create(Registries.RECIPE_SERIALIZER, ResourceLocation.parse("integrateddynamics:squeezer"));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RecipeMechanicalSqueezer>> RECIPESERIALIZER_MECHANICAL_SQUEEZER = DeferredHolder.create(Registries.RECIPE_SERIALIZER, ResourceLocation.parse("integrateddynamics:mechanical_squeezer"));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RecipeNbtClear>> RECIPESERIALIZER_NBT_CLEAR = DeferredHolder.create(Registries.RECIPE_SERIALIZER, ResourceLocation.parse("integrateddynamics:crafting_special_nbt_clear"));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RecipeEnergyContainerCombination>> RECIPESERIALIZER_ENERGY_CONTAINER_COMBINATION = DeferredHolder.create(Registries.RECIPE_SERIALIZER, ResourceLocation.parse("integrateddynamics:crafting_special_energycontainer_combination"));
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<ItemFacadeRecipe>> RECIPESERIALIZER_FACADE = DeferredHolder.create(Registries.RECIPE_SERIALIZER, ResourceLocation.parse("integrateddynamics:crafting_special_facade"));
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<ItemVariableCopyRecipe>> RECIPESERIALIZER_VARIABLE_COPY = DeferredHolder.create(Registries.RECIPE_SERIALIZER, ResourceLocation.parse("integrateddynamics:crafting_special_variable_copy"));

    public static final DeferredHolder<SoundEvent, SoundEvent> SOUNDEVENT_EFFECT_PAGE_FLIPSINGLE = DeferredHolder.create(Registries.SOUND_EVENT, ResourceLocation.parse("integrateddynamics:effect_page_flipsingle"));
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUNDEVENT_EFFECT_PAGE_FLIPMULTIPLE = DeferredHolder.create(Registries.SOUND_EVENT, ResourceLocation.parse("integrateddynamics:effect_page_flipmultiple"));

    public static final DeferredHolder<EntityType<?>, EntityType<? extends EntityItemTargetted>> ENTITYTYPE_ITEM_TARGETTED = DeferredHolder.create(Registries.ENTITY_TYPE, ResourceLocation.parse("integrateddynamics:item_targetted"));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockState>> DATACOMPONENT_FACADE_BLOCK = DeferredHolder.create(Registries.DATA_COMPONENT_TYPE, ResourceLocation.parse("integrateddynamics:facade_block"));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Pair<SimpleFluidContent, SimpleFluidContent>>> DATACOMPONENT_FLUID_CONTENT_IN_OUT = DeferredHolder.create(Registries.DATA_COMPONENT_TYPE, ResourceLocation.parse("integrateddynamics:fluid_content_in_out"));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> DATACOMPONENT_OMNIDIRECTIONAL_GROUP = DeferredHolder.create(Registries.DATA_COMPONENT_TYPE, ResourceLocation.parse("integrateddynamics:omnidirectional_group"));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> DATACOMPONENT_PART_ENHANCEMENT = DeferredHolder.create(Registries.DATA_COMPONENT_TYPE, ResourceLocation.parse("integrateddynamics:part_enhancement"));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> DATACOMPONENT_PART_STATE = DeferredHolder.create(Registries.DATA_COMPONENT_TYPE, ResourceLocation.parse("integrateddynamics:part_state"));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> DATACOMPONENT_PROXY_ID = DeferredHolder.create(Registries.DATA_COMPONENT_TYPE, ResourceLocation.parse("integrateddynamics:proxy_id"));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> DATACOMPONENT_VARIABLE_FACADE = DeferredHolder.create(Registries.DATA_COMPONENT_TYPE, ResourceLocation.parse("integrateddynamics:variable_facade"));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> DATACOMPONENT_WRENCH_TARGET_BLOCKPOS = DeferredHolder.create(Registries.DATA_COMPONENT_TYPE, ResourceLocation.parse("integrateddynamics:wrench_target_blockpos"));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Direction>> DATACOMPONENT_WRENCH_TARGET_DIRECTION = DeferredHolder.create(Registries.DATA_COMPONENT_TYPE, ResourceLocation.parse("integrateddynamics:wrench_target_direction"));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemWrench.Mode>> DATACOMPONENT_WRENCH_MODE = DeferredHolder.create(Registries.DATA_COMPONENT_TYPE, ResourceLocation.parse("integrateddynamics:wrench_mode"));


}
