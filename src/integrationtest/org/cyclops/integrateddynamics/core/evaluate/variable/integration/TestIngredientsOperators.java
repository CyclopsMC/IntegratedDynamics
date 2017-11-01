package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.http.util.Asserts;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeIngredientItemStack;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeIngredients;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.operator.PredicateOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.IIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.IngredientsRecipeIngredientsWrapper;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.RecipeIngredientEnergy;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.RecipeIngredientFluidStack;
import org.cyclops.integrateddynamics.core.test.IntegrationBefore;
import org.cyclops.integrateddynamics.core.test.IntegrationTest;
import org.cyclops.integrateddynamics.core.test.TestHelpers;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Test the different ingredients operators.
 * @author rubensworks
 */
public class TestIngredientsOperators {

    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariable<ValueTypeInteger.ValueInteger> i0;
    private DummyVariable<ValueTypeInteger.ValueInteger> i1;
    private DummyVariable<ValueTypeInteger.ValueInteger> i2;
    private DummyVariable<ValueTypeInteger.ValueInteger> i3;

    private DummyVariableIngredients iEmpty;
    private DummyVariableIngredients iItems;
    private DummyVariableIngredients iFluids;
    private DummyVariableIngredients iEnergies;
    private IIngredients inputIngredients;
    private DummyVariableIngredients iMix;

    private DummyVariable<ValueTypeList.ValueList> lItems;
    private Predicate<ValueObjectTypeItemStack.ValueItemStack> pItemRaw;
    private DummyVariable<ValueTypeOperator.ValueOperator> pItem;
    
    private DummyVariable<ValueTypeList.ValueList> lFluids;
    private Predicate<ValueObjectTypeFluidStack.ValueFluidStack> pFluidRaw;
    private DummyVariable<ValueTypeOperator.ValueOperator> pFluid;

    private DummyVariable<ValueTypeList.ValueList> lEnergies;
    private Predicate<ValueTypeInteger.ValueInteger> pEnergyRaw;
    private DummyVariable<ValueTypeOperator.ValueOperator> pEnergy;

    @IntegrationBefore
    public void before() {
        i0 = new DummyVariable<>(ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(0));
        i1 = new DummyVariable<>(ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(1));
        i2 = new DummyVariable<>(ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(2));
        i3 = new DummyVariable<>(ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(3));

        iEmpty = new DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients.of(
                new IngredientsRecipeIngredientsWrapper(new RecipeIngredients())));

        iItems = new DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients.of(
                new IngredientsRecipeIngredientsWrapper(new RecipeIngredients(
                        new RecipeIngredientItemStack(Ingredient.EMPTY),
                        new RecipeIngredientItemStack(Ingredient.fromItem(Items.BOAT)),
                        new RecipeIngredientItemStack(Ingredient.fromItem(Item.getItemFromBlock(Blocks.STONE))),
                        new RecipeIngredientItemStack(Ingredient.EMPTY)
                ))));

        iFluids = new DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients.of(
                new IngredientsRecipeIngredientsWrapper(new RecipeIngredients(
                        new RecipeIngredientFluidStack(new FluidStack(FluidRegistry.LAVA, 1000)),
                        new RecipeIngredientFluidStack(new FluidStack(FluidRegistry.WATER, 123))
                ))));

        iEnergies = new DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients.of(
                new IngredientsRecipeIngredientsWrapper(new RecipeIngredients(
                        new RecipeIngredientEnergy(666),
                        new RecipeIngredientEnergy(777),
                        new RecipeIngredientEnergy(0)
                ))));

        inputIngredients = new IngredientsRecipeIngredientsWrapper(new RecipeIngredients(
                new RecipeIngredientEnergy(777),
                new RecipeIngredientFluidStack(new FluidStack(FluidRegistry.WATER, 123)),
                new RecipeIngredientItemStack(Ingredient.fromItem(Items.BOAT)),
                new RecipeIngredientItemStack(Ingredient.fromItem(Item.getItemFromBlock(Blocks.STONE)))
        ));
        iMix = new DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients.of(inputIngredients));

        lItems = new DummyVariable<>(ValueTypes.LIST, ValueTypeList.ValueList.ofAll(
                ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.APPLE)),
                ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.APPLE, 1, 1))
        ));
        pItemRaw = ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.APPLE))::equals;
        pItem = new DummyVariable<>(ValueTypes.OPERATOR, ValueTypeOperator.ValueOperator.of(new PredicateOperator<>(
                pItemRaw, ValueTypes.OBJECT_ITEMSTACK, Collections.emptyList())));

        lFluids = new DummyVariable<>(ValueTypes.LIST, ValueTypeList.ValueList.ofAll(
                ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(FluidRegistry.WATER, 123)),
                ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(FluidRegistry.WATER, 456))
        ));
        pFluidRaw = ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(FluidRegistry.WATER, 123))::equals;
        pFluid = new DummyVariable<>(ValueTypes.OPERATOR, ValueTypeOperator.ValueOperator.of(new PredicateOperator<>(
                pFluidRaw, ValueTypes.OBJECT_FLUIDSTACK, Collections.emptyList())));

        lEnergies = new DummyVariable<>(ValueTypes.LIST, ValueTypeList.ValueList.ofAll(
                ValueTypeInteger.ValueInteger.of(123),
                ValueTypeInteger.ValueInteger.of(456)
        ));
        pEnergyRaw = ValueTypeInteger.ValueInteger.of(123)::equals;
        pEnergy = new DummyVariable<>(ValueTypes.OPERATOR, ValueTypeOperator.ValueOperator.of(new PredicateOperator<>(
                pEnergyRaw, ValueTypes.INTEGER, Collections.emptyList())));
    }

    /**
     * ----------------------------------- ITEM_SIZE -----------------------------------
     */

    @IntegrationTest
    public void testItemSize() throws EvaluationException {
        IValue res1 = Operators.INGREDIENTS_ITEM_SIZE.evaluate(new IVariable[]{iMix});
        Asserts.check(res1 instanceof ValueTypeInteger.ValueInteger, "result is an integer");
        TestHelpers.assertEqual(((ValueTypeInteger.ValueInteger) res1).getRawValue(), 2, "item_size(mix) = 2");

        IValue res2 = Operators.INGREDIENTS_ITEM_SIZE.evaluate(new IVariable[]{iItems});
        TestHelpers.assertEqual(((ValueTypeInteger.ValueInteger) res2).getRawValue(), 4, "item_size(items) = 4");

        IValue res3 = Operators.INGREDIENTS_ITEM_SIZE.evaluate(new IVariable[]{iFluids});
        TestHelpers.assertEqual(((ValueTypeInteger.ValueInteger) res3).getRawValue(), 0, "item_size(fluids) = 0");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testItemSizeSizeLarge() throws EvaluationException {
        Operators.INGREDIENTS_ITEM_SIZE.evaluate(new IVariable[]{iMix, iMix});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testItemSizeSizeSmall() throws EvaluationException {
        Operators.INGREDIENTS_ITEM_SIZE.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testItemSizeSize() throws EvaluationException {
        Operators.INGREDIENTS_ITEM_SIZE.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- ITEMS -----------------------------------
     */

    @IntegrationTest
    public void testItems() throws EvaluationException {
        IValue res1 = Operators.INGREDIENTS_ITEMS.evaluate(new IVariable[]{iMix, i0});
        Asserts.check(res1 instanceof ValueTypeList.ValueList, "result is a list");
        TestHelpers.assertEqual(((ValueTypeList.ValueList) res1).getRawValue().getLength(), 1, "items(mix, 0).size = 1");
        TestHelpers.assertEqual(((ValueTypeList.ValueList<ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack>) res1)
                .getRawValue().get(0).getRawValue().getItem(), Items.BOAT, "items(mix, 0) = boat");

        IValue res2 = Operators.INGREDIENTS_ITEMS.evaluate(new IVariable[]{iMix, i1});
        TestHelpers.assertEqual(((ValueTypeList.ValueList) res2).getRawValue().getLength(), 1, "items(mix, 1).size = 1");
        TestHelpers.assertEqual(((ValueTypeList.ValueList<ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack>) res2)
                .getRawValue().get(0).getRawValue().getItem(), Item.getItemFromBlock(Blocks.STONE), "items(mix, 1) = stone");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testItemsOutOfBounds() throws EvaluationException {
        Operators.INGREDIENTS_ITEMS.evaluate(new IVariable[]{iMix, i2});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testItemsSizeLarge() throws EvaluationException {
        Operators.INGREDIENTS_ITEMS.evaluate(new IVariable[]{iMix, i0, i0});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testItemsSizeSmall() throws EvaluationException {
        Operators.INGREDIENTS_ITEMS.evaluate(new IVariable[]{iMix});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testItemsSize() throws EvaluationException {
        Operators.INGREDIENTS_ITEMS.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- ITEM_PREDICATE -----------------------------------
     */

    @IntegrationTest
    public void testItemPredicate() throws EvaluationException {
        IValue res1 = Operators.INGREDIENTS_ITEM_PREDICATE.evaluate(new IVariable[]{iMix, i0});
        Asserts.check(res1 instanceof ValueTypeOperator.ValueOperator, "result is a predicate");
        TestHelpers.assertEqual((((ValueTypeBoolean.ValueBoolean) ((ValueTypeOperator.ValueOperator) res1).getRawValue().evaluate(
                new DummyVariable(ValueTypes.OBJECT_ITEMSTACK, ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.BOAT)))
        )).getRawValue()), true, "item_predicate(mix, boat) = true");
        TestHelpers.assertEqual((((ValueTypeBoolean.ValueBoolean) ((ValueTypeOperator.ValueOperator) res1).getRawValue().evaluate(
                new DummyVariable(ValueTypes.OBJECT_ITEMSTACK, ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.APPLE)))
        )).getRawValue()), false, "item_predicate(mix, apple) = false");

        IValue res2 = Operators.INGREDIENTS_ITEM_PREDICATE.evaluate(new IVariable[]{iMix, i1});
        TestHelpers.assertEqual((((ValueTypeBoolean.ValueBoolean) ((ValueTypeOperator.ValueOperator) res2).getRawValue().evaluate(
                new DummyVariable(ValueTypes.OBJECT_ITEMSTACK, ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.BOAT)))
        )).getRawValue()), false, "item_predicate(mix, boat) = false");
        TestHelpers.assertEqual((((ValueTypeBoolean.ValueBoolean) ((ValueTypeOperator.ValueOperator) res2).getRawValue().evaluate(
                new DummyVariable(ValueTypes.OBJECT_ITEMSTACK, ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Item.getItemFromBlock(Blocks.STONE))))
        )).getRawValue()), true, "item_predicate(mix, stone) = true");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testItemPredicateOutOfBounds() throws EvaluationException {
        Operators.INGREDIENTS_ITEM_PREDICATE.evaluate(new IVariable[]{iMix, i2});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testItemPredicateSizeLarge() throws EvaluationException {
        Operators.INGREDIENTS_ITEM_PREDICATE.evaluate(new IVariable[]{iMix, i0, i0});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testItemPredicateSizeSmall() throws EvaluationException {
        Operators.INGREDIENTS_ITEM_PREDICATE.evaluate(new IVariable[]{iMix});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testItemPredicateSize() throws EvaluationException {
        Operators.INGREDIENTS_ITEM_PREDICATE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- FLUID_SIZE -----------------------------------
     */

    @IntegrationTest
    public void testFluidSize() throws EvaluationException {
        IValue res1 = Operators.INGREDIENTS_FLUID_SIZE.evaluate(new IVariable[]{iMix});
        Asserts.check(res1 instanceof ValueTypeInteger.ValueInteger, "result is an integer");
        TestHelpers.assertEqual(((ValueTypeInteger.ValueInteger) res1).getRawValue(), 1, "fluid_size(mix) = 1");

        IValue res2 = Operators.INGREDIENTS_FLUID_SIZE.evaluate(new IVariable[]{iItems});
        TestHelpers.assertEqual(((ValueTypeInteger.ValueInteger) res2).getRawValue(), 0, "fluid_size(items) = 0");

        IValue res3 = Operators.INGREDIENTS_FLUID_SIZE.evaluate(new IVariable[]{iFluids});
        TestHelpers.assertEqual(((ValueTypeInteger.ValueInteger) res3).getRawValue(), 2, "fluid_size(fluids) = 2");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testFluidSizeSizeLarge() throws EvaluationException {
        Operators.INGREDIENTS_FLUID_SIZE.evaluate(new IVariable[]{iMix, iMix});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testFluidSizeSizeSmall() throws EvaluationException {
        Operators.INGREDIENTS_FLUID_SIZE.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testFluidSizeSize() throws EvaluationException {
        Operators.INGREDIENTS_FLUID_SIZE.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- FLUIDS -----------------------------------
     */

    @IntegrationTest
    public void testFluids() throws EvaluationException {
        IValue res1 = Operators.INGREDIENTS_FLUIDS.evaluate(new IVariable[]{iFluids, i0});
        Asserts.check(res1 instanceof ValueTypeList.ValueList, "result is a list");
        TestHelpers.assertEqual(((ValueTypeList.ValueList) res1).getRawValue().getLength(), 1, "fluids(fluids, 0).size = 1");
        TestHelpers.assertEqual(((ValueTypeList.ValueList<ValueObjectTypeFluidStack, ValueObjectTypeFluidStack.ValueFluidStack>) res1)
                .getRawValue().get(0).getRawValue().get(), new FluidStack(FluidRegistry.LAVA, 1000), "fluids(fluids, 0) = lava");

        IValue res2 = Operators.INGREDIENTS_FLUIDS.evaluate(new IVariable[]{iFluids, i1});
        TestHelpers.assertEqual(((ValueTypeList.ValueList) res2).getRawValue().getLength(), 1, "fluids(fluids, 1).size = 1");
        TestHelpers.assertEqual(((ValueTypeList.ValueList<ValueObjectTypeFluidStack, ValueObjectTypeFluidStack.ValueFluidStack>) res2)
                .getRawValue().get(0).getRawValue().get(), new FluidStack(FluidRegistry.WATER, 123), "fluids(fluids, 2) = water");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testFluidsOutOfBounds() throws EvaluationException {
        Operators.INGREDIENTS_FLUIDS.evaluate(new IVariable[]{iMix, i2});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testFluidsSizeLarge() throws EvaluationException {
        Operators.INGREDIENTS_FLUIDS.evaluate(new IVariable[]{iMix, i0, i0});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testFluidsSizeSmall() throws EvaluationException {
        Operators.INGREDIENTS_FLUIDS.evaluate(new IVariable[]{iMix});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testFluidsSize() throws EvaluationException {
        Operators.INGREDIENTS_FLUIDS.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- FLUID_PREDICATE -----------------------------------
     */

    @IntegrationTest
    public void testFluidPredicate() throws EvaluationException {
        IValue res1 = Operators.INGREDIENTS_FLUID_PREDICATE.evaluate(new IVariable[]{iFluids, i0});
        Asserts.check(res1 instanceof ValueTypeOperator.ValueOperator, "result is a predicate");
        TestHelpers.assertEqual((((ValueTypeBoolean.ValueBoolean) ((ValueTypeOperator.ValueOperator) res1).getRawValue().evaluate(
                new DummyVariable(ValueTypes.OBJECT_FLUIDSTACK, ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(FluidRegistry.LAVA, 1000)))
        )).getRawValue()), true, "fluid_predicate(mix, lava:1000) = true");
        TestHelpers.assertEqual((((ValueTypeBoolean.ValueBoolean) ((ValueTypeOperator.ValueOperator) res1).getRawValue().evaluate(
                new DummyVariable(ValueTypes.OBJECT_FLUIDSTACK, ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(FluidRegistry.WATER, 123)))
        )).getRawValue()), false, "fluid_predicate(mix, water:123) = false");

        IValue res2 = Operators.INGREDIENTS_FLUID_PREDICATE.evaluate(new IVariable[]{iFluids, i1});
        TestHelpers.assertEqual((((ValueTypeBoolean.ValueBoolean) ((ValueTypeOperator.ValueOperator) res2).getRawValue().evaluate(
                new DummyVariable(ValueTypes.OBJECT_FLUIDSTACK, ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(FluidRegistry.WATER, 123)))
        )).getRawValue()), true, "fluid_predicate(mix, water:123) = true");
        TestHelpers.assertEqual((((ValueTypeBoolean.ValueBoolean) ((ValueTypeOperator.ValueOperator) res2).getRawValue().evaluate(
                new DummyVariable(ValueTypes.OBJECT_FLUIDSTACK, ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(FluidRegistry.WATER, 1000)))
        )).getRawValue()), false, "fluid_predicate(mix, water:1000) = false");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testFluidPredicateOutOfBounds() throws EvaluationException {
        Operators.INGREDIENTS_FLUID_PREDICATE.evaluate(new IVariable[]{iMix, i2});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testFluidPredicateSizeLarge() throws EvaluationException {
        Operators.INGREDIENTS_FLUID_PREDICATE.evaluate(new IVariable[]{iMix, i0, i0});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testFluidPredicateSizeSmall() throws EvaluationException {
        Operators.INGREDIENTS_FLUID_PREDICATE.evaluate(new IVariable[]{iMix});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testFluidPredicateSize() throws EvaluationException {
        Operators.INGREDIENTS_FLUID_PREDICATE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- ENERGY_SIZE -----------------------------------
     */

    @IntegrationTest
    public void testEnergySize() throws EvaluationException {
        IValue res1 = Operators.INGREDIENTS_ENERGY_SIZE.evaluate(new IVariable[]{iMix});
        Asserts.check(res1 instanceof ValueTypeInteger.ValueInteger, "result is an integer");
        TestHelpers.assertEqual(((ValueTypeInteger.ValueInteger) res1).getRawValue(), 1, "energy_size(mix) = 1");

        IValue res2 = Operators.INGREDIENTS_ENERGY_SIZE.evaluate(new IVariable[]{iItems});
        TestHelpers.assertEqual(((ValueTypeInteger.ValueInteger) res2).getRawValue(), 0, "energy_size(items) = 0");

        IValue res3 = Operators.INGREDIENTS_ENERGY_SIZE.evaluate(new IVariable[]{iEnergies});
        TestHelpers.assertEqual(((ValueTypeInteger.ValueInteger) res3).getRawValue(), 3, "energy_size(energies) = 3");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testEnergySizeSizeLarge() throws EvaluationException {
        Operators.INGREDIENTS_ENERGY_SIZE.evaluate(new IVariable[]{iMix, iMix});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testEnergySizeSizeSmall() throws EvaluationException {
        Operators.INGREDIENTS_ENERGY_SIZE.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testEnergySizeSize() throws EvaluationException {
        Operators.INGREDIENTS_ENERGY_SIZE.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- ENERGIES -----------------------------------
     */

    @IntegrationTest
    public void testEnergies() throws EvaluationException {
        IValue res1 = Operators.INGREDIENTS_ENERGIES.evaluate(new IVariable[]{iEnergies, i0});
        Asserts.check(res1 instanceof ValueTypeList.ValueList, "result is a list");
        TestHelpers.assertEqual(((ValueTypeList.ValueList) res1).getRawValue().getLength(), 1, "energys(energys, 0).size = 1");
        TestHelpers.assertEqual(((ValueTypeList.ValueList<ValueTypeInteger, ValueTypeInteger.ValueInteger>) res1)
                .getRawValue().get(0).getRawValue(), 666, "energies(energies, 0) = 666");

        IValue res2 = Operators.INGREDIENTS_ENERGIES.evaluate(new IVariable[]{iEnergies, i1});
        TestHelpers.assertEqual(((ValueTypeList.ValueList) res2).getRawValue().getLength(), 1, "energys(energys, 1).size = 1");
        TestHelpers.assertEqual(((ValueTypeList.ValueList<ValueTypeInteger, ValueTypeInteger.ValueInteger>) res2)
                .getRawValue().get(0).getRawValue(), 777, "energies(energies, 1) = 777");

        IValue res3 = Operators.INGREDIENTS_ENERGIES.evaluate(new IVariable[]{iEnergies, i2});
        TestHelpers.assertEqual(((ValueTypeList.ValueList) res3).getRawValue().getLength(), 1, "energys(energys, 2).size = 1");
        TestHelpers.assertEqual(((ValueTypeList.ValueList<ValueTypeInteger, ValueTypeInteger.ValueInteger>) res3)
                .getRawValue().get(0).getRawValue(), 0, "energies(energies, 2) = 0");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testEnergiesOutOfBounds() throws EvaluationException {
        Operators.INGREDIENTS_ENERGIES.evaluate(new IVariable[]{iMix, i2});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testEnergiesSizeLarge() throws EvaluationException {
        Operators.INGREDIENTS_ENERGIES.evaluate(new IVariable[]{iMix, i0, i0});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testEnergiesSizeSmall() throws EvaluationException {
        Operators.INGREDIENTS_ENERGIES.evaluate(new IVariable[]{iMix});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testEnergiesSize() throws EvaluationException {
        Operators.INGREDIENTS_ENERGIES.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- ENERGY_PREDICATE -----------------------------------
     */

    @IntegrationTest
    public void testEnergyPredicate() throws EvaluationException {
        IValue res1 = Operators.INGREDIENTS_ENERGY_PREDICATE.evaluate(new IVariable[]{iEnergies, i0});
        Asserts.check(res1 instanceof ValueTypeOperator.ValueOperator, "result is a predicate");
        TestHelpers.assertEqual((((ValueTypeBoolean.ValueBoolean) ((ValueTypeOperator.ValueOperator) res1).getRawValue().evaluate(
                new DummyVariable(ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(666))
        )).getRawValue()), true, "energy_predicate(mix, 666) = true");
        TestHelpers.assertEqual((((ValueTypeBoolean.ValueBoolean) ((ValueTypeOperator.ValueOperator) res1).getRawValue().evaluate(
                new DummyVariable(ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(667))
        )).getRawValue()), false, "energy_predicate(mix, 667) = false");

        IValue res2 = Operators.INGREDIENTS_ENERGY_PREDICATE.evaluate(new IVariable[]{iEnergies, i1});
        TestHelpers.assertEqual((((ValueTypeBoolean.ValueBoolean) ((ValueTypeOperator.ValueOperator) res2).getRawValue().evaluate(
                new DummyVariable(ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(777))
        )).getRawValue()), true, "energy_predicate(mix, 777) = true");
        TestHelpers.assertEqual((((ValueTypeBoolean.ValueBoolean) ((ValueTypeOperator.ValueOperator) res2).getRawValue().evaluate(
                new DummyVariable(ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(666))
        )).getRawValue()), false, "energy_predicate(mix, 666) = false");

        IValue res3 = Operators.INGREDIENTS_ENERGY_PREDICATE.evaluate(new IVariable[]{iEnergies, i2});
        TestHelpers.assertEqual((((ValueTypeBoolean.ValueBoolean) ((ValueTypeOperator.ValueOperator) res3).getRawValue().evaluate(
                new DummyVariable(ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(0))
        )).getRawValue()), true, "energy_predicate(mix, 0) = true");
        TestHelpers.assertEqual((((ValueTypeBoolean.ValueBoolean) ((ValueTypeOperator.ValueOperator) res3).getRawValue().evaluate(
                new DummyVariable(ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(666))
        )).getRawValue()), false, "energy_predicate(mix, 666) = false");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testEnergyPredicateOutOfBounds() throws EvaluationException {
        Operators.INGREDIENTS_ENERGY_PREDICATE.evaluate(new IVariable[]{iMix, i2});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testEnergyPredicateSizeLarge() throws EvaluationException {
        Operators.INGREDIENTS_ENERGY_PREDICATE.evaluate(new IVariable[]{iMix, i0, i0});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testEnergyPredicateSizeSmall() throws EvaluationException {
        Operators.INGREDIENTS_ENERGY_PREDICATE.evaluate(new IVariable[]{iMix});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testEnergyPredicateSize() throws EvaluationException {
        Operators.INGREDIENTS_ENERGY_PREDICATE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_ITEMS -----------------------------------
     */

    @IntegrationTest
    public void testWithItems() throws EvaluationException {
        IValue res1 = Operators.INGREDIENTS_WITH_ITEMS.evaluate(new IVariable[]{iMix, i0, lItems});
        Asserts.check(res1 instanceof ValueObjectTypeIngredients.ValueIngredients, "result is an ingredient");
        IIngredients outputIngredients1 = ((ValueObjectTypeIngredients.ValueIngredients) res1).getRawValue().get();
        List<ValueObjectTypeItemStack.ValueItemStack> outputList1 = outputIngredients1.getItemStacks(0);
        TestHelpers.assertEqual(outputList1.size(), 2, "with_items(mix, 0, items)[0]size = 2");
        TestHelpers.assertEqual(outputList1.get(0), ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.APPLE)),
                "with_items(mix, 0, items)[0][0] = items[0]");
        TestHelpers.assertEqual(outputList1.get(1), ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.APPLE, 1, 1)),
                "with_items(mix, 0, items)[0][0] = items[0]");

        TestHelpers.assertEqual(outputIngredients1.getItemStacksRaw().size(), inputIngredients.getItemStacksRaw().size(), "Items size remains the same");
        TestHelpers.assertNonEqual(outputIngredients1.getItemStacksRaw().get(0), inputIngredients.getItemStacksRaw().get(0), "Items 0 changes");
        TestHelpers.assertEqual(outputIngredients1.getItemStacksRaw().get(1), inputIngredients.getItemStacksRaw().get(1), "Items 1 remains the same");
        TestHelpers.assertEqual(outputIngredients1.getFluidStacksRaw(), inputIngredients.getFluidStacksRaw(), "Fluid remains the same");
        TestHelpers.assertEqual(outputIngredients1.getEnergiesRaw(), inputIngredients.getEnergiesRaw(), "Energy remains the same");


        IValue res2 = Operators.INGREDIENTS_WITH_ITEMS.evaluate(new IVariable[]{iMix, i3, lItems});
        IIngredients outputIngredients2 = ((ValueObjectTypeIngredients.ValueIngredients) res2).getRawValue().get();
        List<ValueObjectTypeItemStack.ValueItemStack> outputList2 = outputIngredients2.getItemStacks(3);
        TestHelpers.assertEqual(outputList2.size(), 2, "with_items(mix, 3, items)[0]size = 2");
        TestHelpers.assertEqual(outputList2.get(0), ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.APPLE)),
                "with_items(mix, 3, items)[3][0] = items[0]");
        TestHelpers.assertEqual(outputList2.get(1), ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.APPLE, 1, 1)),
                "with_items(mix, 3, items)[3][0] = items[0]");

        TestHelpers.assertNonEqual(outputIngredients2.getItemStacksRaw().size(), inputIngredients.getItemStacksRaw().size(), "Items size changes");
        TestHelpers.assertEqual(outputIngredients2.getItemStacksRaw().get(0), inputIngredients.getItemStacksRaw().get(0), "Items 0 remains the same");
        TestHelpers.assertEqual(outputIngredients2.getItemStacksRaw().get(1), inputIngredients.getItemStacksRaw().get(1), "Items 1 remains the same");
        TestHelpers.assertEqual(outputIngredients2.getItemStacksRaw().get(2), Collections.emptyList(), "Items 2 is empty");
        TestHelpers.assertNonEqual(outputIngredients2.getItemStacksRaw().get(3), Collections.emptyList(), "Items 3 is not empty");
        TestHelpers.assertEqual(outputIngredients2.getFluidStacksRaw(), inputIngredients.getFluidStacksRaw(), "Fluid remains the same");
        TestHelpers.assertEqual(outputIngredients2.getEnergiesRaw(), inputIngredients.getEnergiesRaw(), "Energy remains the same");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithItemsSizeLarge() throws EvaluationException {
        Operators.INGREDIENTS_WITH_ITEMS.evaluate(new IVariable[]{iMix, i0, lItems});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithItemsSizeSmall() throws EvaluationException {
        Operators.INGREDIENTS_WITH_ITEMS.evaluate(new IVariable[]{iMix, i0});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithItemsSize() throws EvaluationException {
        Operators.INGREDIENTS_WITH_ITEMS.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_ITEM_PREDICATE -----------------------------------
     */

    @IntegrationTest
    public void testWithItemPredicate() throws EvaluationException {
        IValue res1 = Operators.INGREDIENTS_WITH_ITEM_PREDICATE.evaluate(new IVariable[]{iMix, i0, pItem});
        Asserts.check(res1 instanceof ValueObjectTypeIngredients.ValueIngredients, "result is an ingredient");
        IIngredients outputIngredients1 = ((ValueObjectTypeIngredients.ValueIngredients) res1).getRawValue().get();
        Predicate<ValueObjectTypeItemStack.ValueItemStack> outputPredicate1 = outputIngredients1.getItemStackPredicate(0);
        TestHelpers.assertEqual(outputPredicate1.test(ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.APPLE))), true, "Predicate is correct");
        TestHelpers.assertEqual(outputPredicate1.test(ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.APPLE, 1, 1))), false, "Predicate is incorrect");

        TestHelpers.assertEqual(outputIngredients1.getItemStacksRaw().size(), inputIngredients.getItemStacksRaw().size(), "list size remains the same");
        TestHelpers.assertNonEqual(outputIngredients1.getItemStacksRaw().get(0), inputIngredients.getItemStacksRaw().get(0), "list 0 changes");
        TestHelpers.assertEqual(outputIngredients1.getItemStacksRaw().get(1), inputIngredients.getItemStacksRaw().get(1), "list 1 remains the same");
        TestHelpers.assertEqual(outputIngredients1.getFluidStacksRaw(), inputIngredients.getFluidStacksRaw(), "Fluid remains the same");
        TestHelpers.assertEqual(outputIngredients1.getEnergiesRaw(), inputIngredients.getEnergiesRaw(), "Energy remains the same");


        IValue res2 = Operators.INGREDIENTS_WITH_ITEM_PREDICATE.evaluate(new IVariable[]{iMix, i3, pItem});
        IIngredients outputIngredients2 = ((ValueObjectTypeIngredients.ValueIngredients) res2).getRawValue().get();
        Predicate<ValueObjectTypeItemStack.ValueItemStack> outputPredicate2 = outputIngredients2.getItemStackPredicate(3);
        TestHelpers.assertEqual(outputPredicate2.test(ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.APPLE))), true, "Predicate is correct");
        TestHelpers.assertEqual(outputPredicate2.test(ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.APPLE, 1, 1))), false, "Predicate is incorrect");

        TestHelpers.assertNonEqual(outputIngredients2.getItemStacksRaw().size(), inputIngredients.getItemStacksRaw().size(), "list size changes");
        TestHelpers.assertEqual(outputIngredients2.getItemStacksRaw().get(0), inputIngredients.getItemStacksRaw().get(0), "list 0 remains the same");
        TestHelpers.assertEqual(outputIngredients2.getItemStacksRaw().get(1), inputIngredients.getItemStacksRaw().get(1), "list 1 remains the same");
        TestHelpers.assertEqual(outputIngredients2.getItemStacksRaw().get(2), Collections.emptyList(), "list 2 is empty");
        TestHelpers.assertEqual(outputIngredients2.getItemStacksRaw().get(3), Collections.emptyList(), "list 3 is empty");
        TestHelpers.assertEqual(outputIngredients2.getFluidStacksRaw(), inputIngredients.getFluidStacksRaw(), "Fluid remains the same");
        TestHelpers.assertEqual(outputIngredients2.getEnergiesRaw(), inputIngredients.getEnergiesRaw(), "Energy remains the same");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithItemPredicateSizeLarge() throws EvaluationException {
        Operators.INGREDIENTS_WITH_ITEM_PREDICATE.evaluate(new IVariable[]{iMix, i0, pItem});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithItemPredicateSizeSmall() throws EvaluationException {
        Operators.INGREDIENTS_WITH_ITEM_PREDICATE.evaluate(new IVariable[]{iMix, i0});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithItemPredicateSize() throws EvaluationException {
        Operators.INGREDIENTS_WITH_ITEM_PREDICATE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_FLUIDS -----------------------------------
     */

    @IntegrationTest
    public void testWithFluids() throws EvaluationException {
        IValue res1 = Operators.INGREDIENTS_WITH_FLUIDS.evaluate(new IVariable[]{iMix, i0, lFluids});
        Asserts.check(res1 instanceof ValueObjectTypeIngredients.ValueIngredients, "result is an ingredient");
        IIngredients outputIngredients1 = ((ValueObjectTypeIngredients.ValueIngredients) res1).getRawValue().get();
        List<ValueObjectTypeFluidStack.ValueFluidStack> outputList1 = outputIngredients1.getFluidStacks(0);
        TestHelpers.assertEqual(outputList1.size(), 2, "with_fluids(mix, 0, fluids)[0]size = 2");
        TestHelpers.assertEqual(outputList1.get(0), ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(FluidRegistry.WATER, 123)),
                "with_fluids(mix, 0, fluids)[0][0] = fluids[0]");
        TestHelpers.assertEqual(outputList1.get(1), ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(FluidRegistry.WATER, 456)),
                "with_fluids(mix, 0, fluids)[0][0] = fluids[0]");

        TestHelpers.assertEqual(outputIngredients1.getFluidStacksRaw().size(), inputIngredients.getFluidStacksRaw().size(), "Fluids size remains the same");
        TestHelpers.assertNonEqual(outputIngredients1.getFluidStacksRaw().get(0), inputIngredients.getFluidStacksRaw().get(0), "Fluids 0 changes");
        TestHelpers.assertEqual(outputIngredients1.getItemStacksRaw(), inputIngredients.getItemStacksRaw(), "Items remains the same");
        TestHelpers.assertEqual(outputIngredients1.getEnergiesRaw(), inputIngredients.getEnergiesRaw(), "Energy remains the same");


        IValue res2 = Operators.INGREDIENTS_WITH_FLUIDS.evaluate(new IVariable[]{iMix, i3, lFluids});
        IIngredients outputIngredients2 = ((ValueObjectTypeIngredients.ValueIngredients) res2).getRawValue().get();
        List<ValueObjectTypeFluidStack.ValueFluidStack> outputList2 = outputIngredients2.getFluidStacks(3);
        TestHelpers.assertEqual(outputList2.size(), 2, "with_fluids(mix, 3, fluids)[0]size = 2");
        TestHelpers.assertEqual(outputList2.get(0), ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(FluidRegistry.WATER, 123)),
                "with_fluids(mix, 3, fluids)[3][0] = fluids[0]");
        TestHelpers.assertEqual(outputList2.get(1), ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(FluidRegistry.WATER, 456)),
                "with_fluids(mix, 3, fluids)[3][0] = fluids[0]");

        TestHelpers.assertNonEqual(outputIngredients2.getFluidStacksRaw().size(), inputIngredients.getFluidStacksRaw().size(), "Fluids size changes");
        TestHelpers.assertEqual(outputIngredients2.getFluidStacksRaw().get(0), inputIngredients.getFluidStacksRaw().get(0), "Fluids 0 remains the same");
        TestHelpers.assertEqual(outputIngredients2.getFluidStacksRaw().get(1), Collections.emptyList(), "Fluids 2 is empty");
        TestHelpers.assertEqual(outputIngredients2.getFluidStacksRaw().get(2), Collections.emptyList(), "Fluids 2 is empty");
        TestHelpers.assertNonEqual(outputIngredients2.getFluidStacksRaw().get(3), Collections.emptyList(), "Fluids 3 is not empty");
        TestHelpers.assertEqual(outputIngredients2.getItemStacksRaw(), inputIngredients.getItemStacksRaw(), "Items remains the same");
        TestHelpers.assertEqual(outputIngredients2.getEnergiesRaw(), inputIngredients.getEnergiesRaw(), "Energy remains the same");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithFluidsSizeLarge() throws EvaluationException {
        Operators.INGREDIENTS_WITH_FLUIDS.evaluate(new IVariable[]{iMix, i0, lFluids});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithFluidsSizeSmall() throws EvaluationException {
        Operators.INGREDIENTS_WITH_FLUIDS.evaluate(new IVariable[]{iMix, i0});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithFluidsSize() throws EvaluationException {
        Operators.INGREDIENTS_WITH_FLUIDS.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_FLUID_PREDICATE -----------------------------------
     */

    @IntegrationTest
    public void testWithFluidPredicate() throws EvaluationException {
        IValue res1 = Operators.INGREDIENTS_WITH_FLUID_PREDICATE.evaluate(new IVariable[]{iMix, i0, pFluid});
        Asserts.check(res1 instanceof ValueObjectTypeIngredients.ValueIngredients, "result is an ingredient");
        IIngredients outputIngredients1 = ((ValueObjectTypeIngredients.ValueIngredients) res1).getRawValue().get();
        Predicate<ValueObjectTypeFluidStack.ValueFluidStack> outputPredicate1 = outputIngredients1.getFluidStackPredicate(0);
        TestHelpers.assertEqual(outputPredicate1.test(ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(FluidRegistry.WATER, 123))), true, "Predicate is correct");
        TestHelpers.assertEqual(outputPredicate1.test(ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(FluidRegistry.WATER, 124))), false, "Predicate is incorrect");

        TestHelpers.assertEqual(outputIngredients1.getFluidStacksRaw().size(), inputIngredients.getFluidStacksRaw().size(), "list size remains the same");
        TestHelpers.assertNonEqual(outputIngredients1.getFluidStacksRaw().get(0), inputIngredients.getFluidStacksRaw().get(0), "list 0 changes");
        TestHelpers.assertEqual(outputIngredients1.getItemStacksRaw(), inputIngredients.getItemStacksRaw(), "Items remains the same");
        TestHelpers.assertEqual(outputIngredients1.getEnergiesRaw(), inputIngredients.getEnergiesRaw(), "Energy remains the same");


        IValue res2 = Operators.INGREDIENTS_WITH_FLUID_PREDICATE.evaluate(new IVariable[]{iMix, i3, pFluid});
        IIngredients outputIngredients2 = ((ValueObjectTypeIngredients.ValueIngredients) res2).getRawValue().get();
        Predicate<ValueObjectTypeFluidStack.ValueFluidStack> outputPredicate2 = outputIngredients2.getFluidStackPredicate(3);
        TestHelpers.assertEqual(outputPredicate2.test(ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(FluidRegistry.WATER, 123))), true, "Predicate is correct");
        TestHelpers.assertEqual(outputPredicate2.test(ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(FluidRegistry.WATER, 124))), false, "Predicate is incorrect");

        TestHelpers.assertNonEqual(outputIngredients2.getFluidStacksRaw().size(), inputIngredients.getFluidStacksRaw().size(), "list size changes");
        TestHelpers.assertEqual(outputIngredients2.getFluidStacksRaw().get(0), inputIngredients.getFluidStacksRaw().get(0), "list 0 remains the same");
        TestHelpers.assertEqual(outputIngredients2.getFluidStacksRaw().get(1), Collections.emptyList(), "list 1 is empty");
        TestHelpers.assertEqual(outputIngredients2.getFluidStacksRaw().get(2), Collections.emptyList(), "list 2 is empty");
        TestHelpers.assertEqual(outputIngredients2.getFluidStacksRaw().get(3), Collections.emptyList(), "list 3 is empty");
        TestHelpers.assertEqual(outputIngredients2.getItemStacksRaw(), inputIngredients.getItemStacksRaw(), "Items remains the same");
        TestHelpers.assertEqual(outputIngredients2.getEnergiesRaw(), inputIngredients.getEnergiesRaw(), "Energy remains the same");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithFluidPredicateSizeLarge() throws EvaluationException {
        Operators.INGREDIENTS_WITH_FLUID_PREDICATE.evaluate(new IVariable[]{iMix, i0, pFluid});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithFluidPredicateSizeSmall() throws EvaluationException {
        Operators.INGREDIENTS_WITH_FLUID_PREDICATE.evaluate(new IVariable[]{iMix, i0});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithFluidPredicateSize() throws EvaluationException {
        Operators.INGREDIENTS_WITH_FLUID_PREDICATE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_ENERGIES -----------------------------------
     */

    @IntegrationTest
    public void testWithEnergies() throws EvaluationException {
        IValue res1 = Operators.INGREDIENTS_WITH_ENERGIES.evaluate(new IVariable[]{iMix, i0, lEnergies});
        Asserts.check(res1 instanceof ValueObjectTypeIngredients.ValueIngredients, "result is an ingredient");
        IIngredients outputIngredients1 = ((ValueObjectTypeIngredients.ValueIngredients) res1).getRawValue().get();
        List<ValueTypeInteger.ValueInteger> outputList1 = outputIngredients1.getEnergies(0);
        TestHelpers.assertEqual(outputList1.size(), 2, "with_energy(mix, 0, energy)[0]size = 2");
        TestHelpers.assertEqual(outputList1.get(0), ValueTypeInteger.ValueInteger.of(123),
                "with_energy(mix, 0, energy)[0][0] = energy[0]");
        TestHelpers.assertEqual(outputList1.get(1), ValueTypeInteger.ValueInteger.of(456),
                "with_energy(mix, 0, energy)[0][0] = energy[0]");

        TestHelpers.assertEqual(outputIngredients1.getEnergiesRaw().size(), inputIngredients.getEnergiesRaw().size(), "Energies size remains the same");
        TestHelpers.assertNonEqual(outputIngredients1.getEnergiesRaw().get(0), inputIngredients.getEnergiesRaw().get(0), "Energies 0 changes");
        TestHelpers.assertEqual(outputIngredients1.getItemStacksRaw(), inputIngredients.getItemStacksRaw(), "Items remains the same");
        TestHelpers.assertEqual(outputIngredients1.getFluidStacksRaw(), inputIngredients.getFluidStacksRaw(), "Fluids remains the same");


        IValue res2 = Operators.INGREDIENTS_WITH_ENERGIES.evaluate(new IVariable[]{iMix, i3, lEnergies});
        IIngredients outputIngredients2 = ((ValueObjectTypeIngredients.ValueIngredients) res2).getRawValue().get();
        List<ValueTypeInteger.ValueInteger> outputList2 = outputIngredients2.getEnergies(3);
        TestHelpers.assertEqual(outputList2.size(), 2, "with_energy(mix, 3, energy)[0]size = 2");
        TestHelpers.assertEqual(outputList2.get(0), ValueTypeInteger.ValueInteger.of(123),
                "with_energy(mix, 3, energy)[3][0] = energy[0]");
        TestHelpers.assertEqual(outputList2.get(1), ValueTypeInteger.ValueInteger.of(456),
                "with_energy(mix, 3, energy)[3][0] = energy[0]");

        TestHelpers.assertNonEqual(outputIngredients2.getEnergiesRaw().size(), inputIngredients.getEnergiesRaw().size(), "Energies size changes");
        TestHelpers.assertEqual(outputIngredients2.getEnergiesRaw().get(0), inputIngredients.getEnergiesRaw().get(0), "Energies 0 remains the same");
        TestHelpers.assertEqual(outputIngredients2.getEnergiesRaw().get(1), Collections.emptyList(), "Energies 2 is empty");
        TestHelpers.assertEqual(outputIngredients2.getEnergiesRaw().get(2), Collections.emptyList(), "Energies 2 is empty");
        TestHelpers.assertNonEqual(outputIngredients2.getEnergiesRaw().get(3), Collections.emptyList(), "Energies 3 is not empty");
        TestHelpers.assertEqual(outputIngredients2.getItemStacksRaw(), inputIngredients.getItemStacksRaw(), "Items remains the same");
        TestHelpers.assertEqual(outputIngredients2.getFluidStacksRaw(), inputIngredients.getFluidStacksRaw(), "Fluids remains the same");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithEnergiesSizeLarge() throws EvaluationException {
        Operators.INGREDIENTS_WITH_ENERGIES.evaluate(new IVariable[]{iMix, i0, lEnergies});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithEnergiesSizeSmall() throws EvaluationException {
        Operators.INGREDIENTS_WITH_ENERGIES.evaluate(new IVariable[]{iMix, i0});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithEnergiesSize() throws EvaluationException {
        Operators.INGREDIENTS_WITH_ENERGIES.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_ENERGY_PREDICATE -----------------------------------
     */

    @IntegrationTest
    public void testWithEnergyPredicate() throws EvaluationException {
        IValue res1 = Operators.INGREDIENTS_WITH_ENERGY_PREDICATE.evaluate(new IVariable[]{iMix, i0, pEnergy});
        Asserts.check(res1 instanceof ValueObjectTypeIngredients.ValueIngredients, "result is an ingredient");
        IIngredients outputIngredients1 = ((ValueObjectTypeIngredients.ValueIngredients) res1).getRawValue().get();
        Predicate<ValueTypeInteger.ValueInteger> outputPredicate1 = outputIngredients1.getEnergiesPredicate(0);
        TestHelpers.assertEqual(outputPredicate1.test(ValueTypeInteger.ValueInteger.of(123)), true, "Predicate is correct");
        TestHelpers.assertEqual(outputPredicate1.test(ValueTypeInteger.ValueInteger.of(124)), false, "Predicate is incorrect");

        TestHelpers.assertEqual(outputIngredients1.getEnergiesRaw().size(), inputIngredients.getEnergiesRaw().size(), "list size remains the same");
        TestHelpers.assertNonEqual(outputIngredients1.getEnergiesRaw().get(0), inputIngredients.getEnergiesRaw().get(0), "list 0 changes");
        TestHelpers.assertEqual(outputIngredients1.getItemStacksRaw(), inputIngredients.getItemStacksRaw(), "Items remains the same");
        TestHelpers.assertEqual(outputIngredients1.getFluidStacksRaw(), inputIngredients.getFluidStacksRaw(), "Fluids remains the same");


        IValue res2 = Operators.INGREDIENTS_WITH_ENERGY_PREDICATE.evaluate(new IVariable[]{iMix, i3, pEnergy});
        IIngredients outputIngredients2 = ((ValueObjectTypeIngredients.ValueIngredients) res2).getRawValue().get();
        Predicate<ValueTypeInteger.ValueInteger> outputPredicate2 = outputIngredients2.getEnergiesPredicate(3);
        TestHelpers.assertEqual(outputPredicate2.test(ValueTypeInteger.ValueInteger.of(123)), true, "Predicate is correct");
        TestHelpers.assertEqual(outputPredicate2.test(ValueTypeInteger.ValueInteger.of(124)), false, "Predicate is incorrect");

        TestHelpers.assertNonEqual(outputIngredients2.getEnergiesRaw().size(), inputIngredients.getEnergiesRaw().size(), "list size changes");
        TestHelpers.assertEqual(outputIngredients2.getEnergiesRaw().get(0), inputIngredients.getEnergiesRaw().get(0), "list 0 remains the same");
        TestHelpers.assertEqual(outputIngredients2.getEnergiesRaw().get(1), Collections.emptyList(), "list 1 is empty");
        TestHelpers.assertEqual(outputIngredients2.getEnergiesRaw().get(2), Collections.emptyList(), "list 2 is empty");
        TestHelpers.assertEqual(outputIngredients2.getEnergiesRaw().get(3), Collections.emptyList(), "list 3 is empty");
        TestHelpers.assertEqual(outputIngredients2.getItemStacksRaw(), inputIngredients.getItemStacksRaw(), "Items remains the same");
        TestHelpers.assertEqual(outputIngredients2.getFluidStacksRaw(), inputIngredients.getFluidStacksRaw(), "Fluids remains the same");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithEnergyPredicateSizeLarge() throws EvaluationException {
        Operators.INGREDIENTS_WITH_ENERGY_PREDICATE.evaluate(new IVariable[]{iMix, i0, pEnergy});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithEnergyPredicateSizeSmall() throws EvaluationException {
        Operators.INGREDIENTS_WITH_ENERGY_PREDICATE.evaluate(new IVariable[]{iMix, i0});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithEnergyPredicateSize() throws EvaluationException {
        Operators.INGREDIENTS_WITH_ENERGY_PREDICATE.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

}
