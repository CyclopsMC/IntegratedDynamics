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
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.IngredientsRecipeIngredientsWrapper;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.RecipeIngredientEnergy;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.RecipeIngredientFluidStack;
import org.cyclops.integrateddynamics.core.test.IntegrationBefore;
import org.cyclops.integrateddynamics.core.test.IntegrationTest;
import org.cyclops.integrateddynamics.core.test.TestHelpers;

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

    private DummyVariableIngredients iEmpty;
    private DummyVariableIngredients iItems;
    private DummyVariableIngredients iFluids;
    private DummyVariableIngredients iEnergies;
    private DummyVariableIngredients iMix;

    @IntegrationBefore
    public void before() {
        i0 = new DummyVariable<>(ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(0));
        i1 = new DummyVariable<>(ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(1));
        i2 = new DummyVariable<>(ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(2));

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

        iMix = new DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients.of(
                new IngredientsRecipeIngredientsWrapper(new RecipeIngredients(
                        new RecipeIngredientEnergy(777),
                        new RecipeIngredientFluidStack(new FluidStack(FluidRegistry.WATER, 123)),
                        new RecipeIngredientItemStack(Ingredient.fromItem(Items.BOAT)),
                        new RecipeIngredientItemStack(Ingredient.fromItem(Item.getItemFromBlock(Blocks.STONE)))
                ))));
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

}
