package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.http.util.Asserts;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeDefinition;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeDefinition;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.MixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeRecipe;
import org.cyclops.integrateddynamics.core.test.IntegrationBefore;
import org.cyclops.integrateddynamics.core.test.IntegrationTest;
import org.cyclops.integrateddynamics.core.test.TestHelpers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Test the different ingredients operators.
 * @author rubensworks
 */
public class TestRecipeOperators {

    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariableRecipe rMain;

    private DummyVariableIngredients iMainOut;
    private DummyVariableIngredients iItems;

    @IntegrationBefore
    public void before() {
        List<List<IPrototypedIngredient<ItemStack, Integer>>> ingredientsIn = Lists.newArrayList();
        ingredientsIn.add(Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, ItemStack.EMPTY, ItemMatch.EXACT)));
        ingredientsIn.add(Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, new ItemStack(Items.BOAT), ItemMatch.EXACT)));
        ingredientsIn.add(Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, new ItemStack(Blocks.STONE), ItemMatch.EXACT)));
        ingredientsIn.add(Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, ItemStack.EMPTY, ItemMatch.EXACT)));

        Map<IngredientComponent<?, ?>, List<?>> ingredientsOut = Maps.newIdentityHashMap();
        ingredientsOut.put(IngredientComponent.ENERGY, Lists.newArrayList(777));
        ingredientsOut.put(IngredientComponent.FLUIDSTACK, Lists.newArrayList(new FluidStack(FluidRegistry.WATER, 123)));
        ingredientsOut.put(IngredientComponent.ITEMSTACK, Lists.newArrayList(new ItemStack(Items.BOAT), new ItemStack(Item.getItemFromBlock(Blocks.STONE))));
        iMainOut = new DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients.of(
                new MixedIngredients(ingredientsOut)));

        rMain = new DummyVariableRecipe(ValueObjectTypeRecipe.ValueRecipe.of(RecipeDefinition.ofIngredients(IngredientComponent.ITEMSTACK,
                ingredientsIn,
                iMainOut.getValue().getRawValue().get()
        )));

        iItems = new DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients.of(
                MixedIngredients.ofInstances(IngredientComponent.ITEMSTACK, Lists.newArrayList(
                        new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack(Blocks.OAK_DOOR), ItemStack.EMPTY)
                )));
    }
    
    /**
     * ----------------------------------- INPUT -----------------------------------
     */

    @IntegrationTest
    public void testInput() throws EvaluationException {
        IValue res1 = Operators.RECIPE_INPUT.evaluate(new IVariable[]{rMain});
        Asserts.check(res1 instanceof ValueObjectTypeIngredients.ValueIngredients, "result is an ingredients");
        TestHelpers.assertEqual(res1, ValueObjectTypeIngredients.ValueIngredients.of(MixedIngredients.fromRecipeInput(rMain.getValue().getRawValue().get())), "input is correct");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInputSizeLarge() throws EvaluationException {
        Operators.RECIPE_INPUT.evaluate(new IVariable[]{rMain, rMain});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInputSizeSmall() throws EvaluationException {
        Operators.RECIPE_INPUT.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testInputSize() throws EvaluationException {
        Operators.RECIPE_INPUT.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- OUTPUT -----------------------------------
     */

    @IntegrationTest
    public void testOutput() throws EvaluationException {
        IValue res1 = Operators.RECIPE_OUTPUT.evaluate(new IVariable[]{rMain});
        Asserts.check(res1 instanceof ValueObjectTypeIngredients.ValueIngredients, "result is an ingredients");
        TestHelpers.assertEqual(res1, iMainOut.getValue(), "output is correct");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testOutputSizeLarge() throws EvaluationException {
        Operators.RECIPE_OUTPUT.evaluate(new IVariable[]{rMain, rMain});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testOutputSizeSmall() throws EvaluationException {
        Operators.RECIPE_OUTPUT.evaluate(new IVariable[]{});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testOutputSize() throws EvaluationException {
        Operators.RECIPE_OUTPUT.evaluate(new IVariable[]{DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_INPUT -----------------------------------
     */

    @IntegrationTest
    public void testWithInput() throws EvaluationException {
        IValue res1 = Operators.RECIPE_WITH_INPUT.evaluate(new IVariable[]{rMain, iItems});
        Asserts.check(res1 instanceof ValueObjectTypeRecipe.ValueRecipe, "result is a recipe");

        List<List<IPrototypedIngredient<ItemStack, Integer>>> ingredientsIn = Lists.newArrayList();
        ingredientsIn.add(Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, new ItemStack(Items.DIAMOND_PICKAXE), ItemMatch.EXACT)));
        ingredientsIn.add(Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, new ItemStack(Blocks.OAK_DOOR), ItemMatch.EXACT)));
        ingredientsIn.add(Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, ItemStack.EMPTY, ItemMatch.EXACT)));
        IRecipeDefinition recipe = RecipeDefinition.ofIngredients(IngredientComponent.ITEMSTACK,
                ingredientsIn,
                iMainOut.getValue().getRawValue().get()
        );
        TestHelpers.assertEqual(res1, ValueObjectTypeRecipe.ValueRecipe.of(recipe), "input is correct");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithInputSizeLarge() throws EvaluationException {
        Operators.RECIPE_WITH_INPUT.evaluate(new IVariable[]{rMain, iItems, rMain});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithInputSizeSmall() throws EvaluationException {
        Operators.RECIPE_WITH_INPUT.evaluate(new IVariable[]{rMain});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithInputSize() throws EvaluationException {
        Operators.RECIPE_WITH_INPUT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_OUTPUT -----------------------------------
     */

    @IntegrationTest
    public void testWithOutput() throws EvaluationException {
        IValue res1 = Operators.RECIPE_WITH_OUTPUT.evaluate(new IVariable[]{rMain, iItems});
        Asserts.check(res1 instanceof ValueObjectTypeRecipe.ValueRecipe, "result is a recipe");

        List<List<IPrototypedIngredient<ItemStack, Integer>>> ingredientsIn = Lists.newArrayList();
        ingredientsIn.add(Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, ItemStack.EMPTY, ItemMatch.EXACT)));
        ingredientsIn.add(Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, new ItemStack(Items.BOAT), ItemMatch.EXACT)));
        ingredientsIn.add(Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, new ItemStack(Blocks.STONE), ItemMatch.EXACT)));
        ingredientsIn.add(Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, ItemStack.EMPTY, ItemMatch.EXACT)));
        IRecipeDefinition recipe = RecipeDefinition.ofIngredients(IngredientComponent.ITEMSTACK,
                ingredientsIn,
                iItems.getValue().getRawValue().get()
        );
        TestHelpers.assertEqual(res1, ValueObjectTypeRecipe.ValueRecipe.of(recipe), "output is correct");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithOutputSizeLarge() throws EvaluationException {
        Operators.RECIPE_WITH_OUTPUT.evaluate(new IVariable[]{rMain, iItems, rMain});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithOutputSizeSmall() throws EvaluationException {
        Operators.RECIPE_WITH_OUTPUT.evaluate(new IVariable[]{rMain});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithOutputSize() throws EvaluationException {
        Operators.RECIPE_WITH_OUTPUT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }

    /**
     * ----------------------------------- WITH_INPUT_OUTPUT -----------------------------------
     */

    @IntegrationTest
    public void testWithInputOutput() throws EvaluationException {
        IValue res1 = Operators.RECIPE_WITH_INPUT_OUTPUT.evaluate(new IVariable[]{iItems, iMainOut});
        Asserts.check(res1 instanceof ValueObjectTypeRecipe.ValueRecipe, "result is a recipe");

        List<List<IPrototypedIngredient<ItemStack, Integer>>> ingredientsIn = Lists.newArrayList();
        ingredientsIn.add(Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, new ItemStack(Items.DIAMOND_PICKAXE), ItemMatch.EXACT)));
        ingredientsIn.add(Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, new ItemStack(Blocks.OAK_DOOR), ItemMatch.EXACT)));
        ingredientsIn.add(Collections.singletonList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, ItemStack.EMPTY, ItemMatch.EXACT)));
        IRecipeDefinition recipe = RecipeDefinition.ofIngredients(IngredientComponent.ITEMSTACK,
                ingredientsIn,
                iMainOut.getValue().getRawValue().get()
        );
        TestHelpers.assertEqual(res1, ValueObjectTypeRecipe.ValueRecipe.of(recipe), "input is correct");
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithInputOutputSizeLarge() throws EvaluationException {
        Operators.RECIPE_WITH_INPUT_OUTPUT.evaluate(new IVariable[]{iItems, iMainOut, rMain});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithInputOutputSizeSmall() throws EvaluationException {
        Operators.RECIPE_WITH_INPUT_OUTPUT.evaluate(new IVariable[]{iItems});
    }

    @IntegrationTest(expected = EvaluationException.class)
    public void testWithInputOutputSize() throws EvaluationException {
        Operators.RECIPE_WITH_INPUT_OUTPUT.evaluate(new IVariable[]{DUMMY_VARIABLE, DUMMY_VARIABLE});
    }
}
