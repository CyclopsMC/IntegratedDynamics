package org.cyclops.integrateddynamics.gametest;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.commoncapabilities.IngredientComponents;
import org.cyclops.commoncapabilities.api.ingredient.IMixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.MixedIngredients;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.blockentity.BlockEntityEnergyBattery;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.*;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsAspectsReadMachine {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 0, 2);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineIsWorkerFalse(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_ISWORKER, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineIsWorkerTrue(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.FURNACE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_ISWORKER, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineHasWorkFalseInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_HASWORK, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineHasWorkFalse(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.FURNACE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_HASWORK, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineHasWorkTrue(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.FURNACE);
        FurnaceBlockEntity machine = helper.getBlockEntity(POS.west());
        machine.setItem(0, new ItemStack(Items.RAW_IRON));
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_HASWORK, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineCanWorkFalseInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_CANWORK, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineCanWorkFalse(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.FURNACE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_CANWORK, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineCanWorkTrue(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.FURNACE);
        FurnaceBlockEntity machine = helper.getBlockEntity(POS.west());
        machine.setItem(1, new ItemStack(Items.COAL));
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_CANWORK, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineIsWorkingFalseInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_ISWORKING, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineIsWorkingFalse(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.FURNACE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_ISWORKING, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineIsWorkingTrue(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.FURNACE);
        FurnaceBlockEntity machine = helper.getBlockEntity(POS.west());
        machine.setItem(0, new ItemStack(Items.RAW_IRON));
        machine.setItem(1, new ItemStack(Items.COAL));
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_ISWORKING, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineIsTemperatureFalse(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_ISTEMPERATURE, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineIsTemperatureTrue(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.FURNACE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_ISTEMPERATURE, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineTemperatureInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.DOUBLE_TEMPERATURE, ValueTypeDouble.ValueDouble.of(0D));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineTemperature(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.FURNACE);
        FurnaceBlockEntity machine = helper.getBlockEntity(POS.west());
        machine.setItem(0, new ItemStack(Items.RAW_IRON));
        machine.setItem(1, new ItemStack(Items.COAL));
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.DOUBLE_TEMPERATURE, ValueTypeDouble.ValueDouble.of(273.15D + 1600D));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineTemperatureWorking(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.FURNACE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.DOUBLE_TEMPERATURE, ValueTypeDouble.ValueDouble.of(273.15D));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineMaxTemperatureInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.DOUBLE_MAXTEMPERATURE, ValueTypeDouble.ValueDouble.of(0D));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineMaxTemperature(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.FURNACE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.DOUBLE_MAXTEMPERATURE, ValueTypeDouble.ValueDouble.of(Double.MAX_VALUE));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineMinTemperatureInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.DOUBLE_MINTEMPERATURE, ValueTypeDouble.ValueDouble.of(0D));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineMinTemperature(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.FURNACE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.DOUBLE_MINTEMPERATURE, ValueTypeDouble.ValueDouble.of(273.15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineDefaultTemperatureInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.DOUBLE_DEFAULTTEMPERATURE, ValueTypeDouble.ValueDouble.of(0D));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineDefaultTemperature(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.FURNACE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.DOUBLE_DEFAULTTEMPERATURE, ValueTypeDouble.ValueDouble.of(273.15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineIsRecipeHandlerFalse(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_ISRECIPEHANDLER, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineIsRecipeHandlerTrue(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.FURNACE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_ISRECIPEHANDLER, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineRecipesInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.LIST_GETRECIPES, ValueTypeList.ValueList.ofAll());
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineRecipes(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.FURNACE);
        testReadAspectPredicate(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.LIST_GETRECIPES, list -> {
            try {
                return list.getRawValue().getLength() > 10;
            } catch (EvaluationException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineGetRecipeOutputInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspectOperator(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.OPERATOR_GETRECIPEOUTPUT,
                Lists.newArrayList(ValueObjectTypeIngredients.ValueIngredients.of(MixedIngredients.ofInstance(IngredientComponents.ITEMSTACK, new ItemStack(Items.RAW_IRON)))),
                ValueObjectTypeIngredients.ValueIngredients.of(null)
        );
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineGetRecipeOutput(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.FURNACE);
        IMixedIngredients ingredients = MixedIngredients.ofInstance(IngredientComponents.ITEMSTACK, new ItemStack(Items.RAW_IRON));
        testReadAspectOperator(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.OPERATOR_GETRECIPEOUTPUT,
                Lists.newArrayList(ValueObjectTypeIngredients.ValueIngredients.of(ingredients)),
                ValueObjectTypeIngredients.ValueIngredients.of(MixedIngredients.ofInstance(IngredientComponents.ITEMSTACK, new ItemStack(Items.IRON_INGOT)))
        );
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineGetRecipeInputsInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspectOperator(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.OPERATOR_GETRECIPEINPUTS,
                Lists.newArrayList(ValueObjectTypeIngredients.ValueIngredients.of(MixedIngredients.ofInstance(IngredientComponents.ITEMSTACK, new ItemStack(Items.IRON_INGOT)))),
                ValueTypeList.ValueList.ofAll()
        );
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineGetRecipeInputs(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.FURNACE);
        IMixedIngredients ingredients = MixedIngredients.ofInstance(IngredientComponents.ITEMSTACK, new ItemStack(Items.IRON_INGOT));
        testReadAspectOperatorPredicate(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.OPERATOR_GETRECIPEINPUTS,
                Lists.newArrayList(ValueObjectTypeIngredients.ValueIngredients.of(ingredients)),
                value -> {
                    try {
                        return value instanceof ValueTypeList.ValueList<?,?> list && list.getRawValue().getLength() >= 3;
                    } catch (EvaluationException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineGetRecipesByInputsInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspectOperator(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.OPERATOR_GETRECIPESBYINPUT,
                Lists.newArrayList(ValueObjectTypeIngredients.ValueIngredients.of(MixedIngredients.ofInstance(IngredientComponents.ITEMSTACK, new ItemStack(Items.RAW_IRON)))),
                ValueTypeList.ValueList.ofAll()
        );
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineGetRecipesByInputsInputs(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.FURNACE);
        IMixedIngredients ingredients = MixedIngredients.ofInstance(IngredientComponents.ITEMSTACK, new ItemStack(Items.RAW_IRON));
        testReadAspectOperatorPredicate(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.OPERATOR_GETRECIPESBYINPUT,
                Lists.newArrayList(ValueObjectTypeIngredients.ValueIngredients.of(ingredients)),
                value -> {
                    try {
                        return value instanceof ValueTypeList.ValueList<?,?> list && list.getRawValue().getLength() == 1;
                    } catch (EvaluationException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineGetRecipesByOutputInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspectOperator(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.OPERATOR_GETRECIPESBYOUTPUT,
                Lists.newArrayList(ValueObjectTypeIngredients.ValueIngredients.of(MixedIngredients.ofInstance(IngredientComponents.ITEMSTACK, new ItemStack(Items.IRON_INGOT)))),
                ValueTypeList.ValueList.ofAll()
        );
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineGetRecipesByOutput(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.FURNACE);
        IMixedIngredients ingredients = MixedIngredients.ofInstance(IngredientComponents.ITEMSTACK, new ItemStack(Items.IRON_INGOT));
        testReadAspectOperatorPredicate(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.OPERATOR_GETRECIPESBYOUTPUT,
                Lists.newArrayList(ValueObjectTypeIngredients.ValueIngredients.of(ingredients)),
                value -> {
                    try {
                        return value instanceof ValueTypeList.ValueList<?,?> list && list.getRawValue().getLength() == 3;
                    } catch (EvaluationException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineGetRecipeByInputsInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspectOperator(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.OPERATOR_GETRECIPEBYINPUT,
                Lists.newArrayList(ValueObjectTypeIngredients.ValueIngredients.of(MixedIngredients.ofInstance(IngredientComponents.ITEMSTACK, new ItemStack(Items.RAW_IRON)))),
                ValueObjectTypeRecipe.ValueRecipe.of(null)
        );
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineGetRecipeByInputsInputs(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.FURNACE);
        IMixedIngredients ingredients = MixedIngredients.ofInstance(IngredientComponents.ITEMSTACK, new ItemStack(Items.RAW_IRON));
        testReadAspectOperatorPredicate(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.OPERATOR_GETRECIPEBYINPUT,
                Lists.newArrayList(ValueObjectTypeIngredients.ValueIngredients.of(ingredients)),
                value -> value instanceof ValueObjectTypeRecipe.ValueRecipe recipe && recipe.getRawValue().isPresent()
        );
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineGetRecipeByOutputInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspectOperator(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.OPERATOR_GETRECIPEBYOUTPUT,
                Lists.newArrayList(ValueObjectTypeIngredients.ValueIngredients.of(MixedIngredients.ofInstance(IngredientComponents.ITEMSTACK, new ItemStack(Items.IRON_INGOT)))),
                ValueObjectTypeRecipe.ValueRecipe.of(null)
        );
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineGetRecipeByOutput(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.FURNACE);
        IMixedIngredients ingredients = MixedIngredients.ofInstance(IngredientComponents.ITEMSTACK, new ItemStack(Items.IRON_INGOT));
        testReadAspectOperatorPredicate(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.OPERATOR_GETRECIPEBYOUTPUT,
                Lists.newArrayList(ValueObjectTypeIngredients.ValueIngredients.of(ingredients)),
                value -> value instanceof ValueObjectTypeRecipe.ValueRecipe recipe && recipe.getRawValue().isPresent()
        );
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineIsEnergyInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_ISENERGY, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineIsEnergyValid(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_ENERGY_BATTERY.get());
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_ISENERGY, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineIsEnergyReceiverInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_ISENERGYRECEIVER, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineIsEnergyReceiverValid(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_ENERGY_BATTERY.get());
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_ISENERGYRECEIVER, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineIsEnergyProviderInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_ISENERGYPROVIDER, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineIsEnergyProviderValid(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_ENERGY_BATTERY.get());
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_ISENERGYPROVIDER, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineCanExtractEnergyInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_CANEXTRACTENERGY, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineCanExtractEnergyValid(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_ENERGY_BATTERY.get());
        BlockEntityEnergyBattery battery = helper.getBlockEntity(POS.west());
        battery.setEnergyStored(100);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_CANEXTRACTENERGY, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineCanExtractEnergyValidFalse(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_ENERGY_BATTERY.get());
        BlockEntityEnergyBattery battery = helper.getBlockEntity(POS.west());
        battery.setEnergyStored(0);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_CANEXTRACTENERGY, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineCanInsertEnergyInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_CANINSERTENERGY, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineCanInsertEnergyValid(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_ENERGY_BATTERY.get());
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_CANINSERTENERGY, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineIsEnergyFullInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_ISENERGYFULL, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineIsEnergyFullValid(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_ENERGY_BATTERY.get());
        BlockEntityEnergyBattery battery = helper.getBlockEntity(POS.west());
        battery.setEnergyStored(1_000_000);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_ISENERGYFULL, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineIsEnergyFullValidFalse(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_ENERGY_BATTERY.get());
        BlockEntityEnergyBattery battery = helper.getBlockEntity(POS.west());
        battery.setEnergyStored(100);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_ISENERGYFULL, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineIsEnergyEmptyInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_ISENERGYEMPTY, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineIsEnergyEmptyValid(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_ENERGY_BATTERY.get());
        BlockEntityEnergyBattery battery = helper.getBlockEntity(POS.west());
        battery.setEnergyStored(0);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_ISENERGYEMPTY, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineIsEnergyEmptyValidFalse(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_ENERGY_BATTERY.get());
        BlockEntityEnergyBattery battery = helper.getBlockEntity(POS.west());
        battery.setEnergyStored(100);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_ISENERGYEMPTY, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineIsEnergyNonEmptyInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_ISENERGYNONEMPTY, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineIsEnergyNonEmptyValid(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_ENERGY_BATTERY.get());
        BlockEntityEnergyBattery battery = helper.getBlockEntity(POS.west());
        battery.setEnergyStored(100);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_ISENERGYNONEMPTY, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineIsEnergyNonEmptyValidFalse(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_ENERGY_BATTERY.get());
        BlockEntityEnergyBattery battery = helper.getBlockEntity(POS.west());
        battery.setEnergyStored(0);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.BOOLEAN_ISENERGYNONEMPTY, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineEnergyStoredInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.INTEGER_ENERGYSTORED, ValueTypeInteger.ValueInteger.of(0));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineEnergyStoredValid(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_ENERGY_BATTERY.get());
        BlockEntityEnergyBattery battery = helper.getBlockEntity(POS.west());
        battery.setEnergyStored(100);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.INTEGER_ENERGYSTORED, ValueTypeInteger.ValueInteger.of(100));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineEnergyCapacityInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.INTEGER_ENERGYCAPACITY, ValueTypeInteger.ValueInteger.of(0));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineEnergyCapacityValid(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_ENERGY_BATTERY.get());
        BlockEntityEnergyBattery battery = helper.getBlockEntity(POS.west());
        battery.setEnergyStored(100);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.INTEGER_ENERGYCAPACITY, ValueTypeInteger.ValueInteger.of(1_000_000));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineEnergyFillRatioInvalid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.DOUBLE_ENERGYFILLRATIO, ValueTypeDouble.ValueDouble.of(0));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadMachineEnergyFillRatioValid(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_ENERGY_BATTERY.get());
        BlockEntityEnergyBattery battery = helper.getBlockEntity(POS.west());
        battery.setEnergyStored(500_000);
        testReadAspect(POS, helper, PartTypes.MACHINE_READER, Aspects.Read.Machine.DOUBLE_ENERGYFILLRATIO, ValueTypeDouble.ValueDouble.of(0.5));
    }

}
