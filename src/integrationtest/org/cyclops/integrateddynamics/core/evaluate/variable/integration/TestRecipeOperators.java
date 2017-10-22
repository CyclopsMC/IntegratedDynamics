package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
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
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeRecipe;
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
public class TestRecipeOperators {

    private static final DummyValueType DUMMY_TYPE = DummyValueType.TYPE;
    private static final DummyVariable<DummyValueType.DummyValue> DUMMY_VARIABLE =
            new DummyVariable<DummyValueType.DummyValue>(DUMMY_TYPE, DummyValueType.DummyValue.of());

    private DummyVariableRecipe rMain;
    
    private DummyVariableIngredients iMainIn;
    private DummyVariableIngredients iMainOut;

    @IntegrationBefore
    public void before() {
        iMainIn = new DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients.of(
                new IngredientsRecipeIngredientsWrapper(new RecipeIngredients(
                        new RecipeIngredientItemStack(Ingredient.EMPTY),
                        new RecipeIngredientItemStack(Ingredient.fromItem(Items.BOAT)),
                        new RecipeIngredientItemStack(Ingredient.fromItem(Item.getItemFromBlock(Blocks.STONE))),
                        new RecipeIngredientItemStack(Ingredient.EMPTY)
                ))));

        iMainOut = new DummyVariableIngredients(ValueObjectTypeIngredients.ValueIngredients.of(
                new IngredientsRecipeIngredientsWrapper(new RecipeIngredients(
                        new RecipeIngredientEnergy(777),
                        new RecipeIngredientFluidStack(new FluidStack(FluidRegistry.WATER, 123)),
                        new RecipeIngredientItemStack(Ingredient.fromItem(Items.BOAT)),
                        new RecipeIngredientItemStack(Ingredient.fromItem(Item.getItemFromBlock(Blocks.STONE)))
                ))));

        rMain = new DummyVariableRecipe(ValueObjectTypeRecipe.ValueRecipe.of(new ValueObjectTypeRecipe.Recipe(
                iMainIn.getValue(),
                iMainOut.getValue()
        )));
    }
    
    /**
     * ----------------------------------- INPUT -----------------------------------
     */

    @IntegrationTest
    public void testInput() throws EvaluationException {
        IValue res1 = Operators.RECIPE_INPUT.evaluate(new IVariable[]{rMain});
        Asserts.check(res1 instanceof ValueObjectTypeIngredients.ValueIngredients, "result is an ingredients");
        TestHelpers.assertEqual(res1, iMainIn, "input is correct");
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
        TestHelpers.assertEqual(res1, iMainOut, "output is correct");
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
}
