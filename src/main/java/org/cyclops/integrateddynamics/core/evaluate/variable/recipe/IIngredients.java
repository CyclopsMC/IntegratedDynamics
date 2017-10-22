package org.cyclops.integrateddynamics.core.evaluate.variable.recipe;

import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;

import java.util.List;
import java.util.function.Predicate;

/**
 * For storing recipe ingredient information internally.
 * @author rubensworks
 */
public interface IIngredients {

    public int getItemStackIngredients();
    public List<ValueObjectTypeItemStack.ValueItemStack> getItemStacks(int index);
    public Predicate<ValueObjectTypeItemStack.ValueItemStack> getItemStackPredicate(int index);
    public List<List<ValueObjectTypeItemStack.ValueItemStack>> getItemStacksRaw();

    public int getFluidStackIngredients();
    public List<ValueObjectTypeFluidStack.ValueFluidStack> getFluidStacks(int index);
    public Predicate<ValueObjectTypeFluidStack.ValueFluidStack> getFluidStackPredicate(int index);
    public List<List<ValueObjectTypeFluidStack.ValueFluidStack>> getFluidStacksRaw();

    public int getEnergyIngredients();
    public List<ValueTypeInteger.ValueInteger> getEnergies(int index);
    public Predicate<ValueTypeInteger.ValueInteger> getEnergiesPredicate(int index);
    public List<List<ValueTypeInteger.ValueInteger>> getEnergiesRaw();

    public static RecipeIngredients toRecipeIngredients(IIngredients ingredients) {
        if (ingredients instanceof IngredientsRecipeIngredientsWrapper) {
            return ((IngredientsRecipeIngredientsWrapper) ingredients).getRecipeIngredients();
        } else {
            return new RecipeIngredientsIngredientsWrapper(ingredients);
        }
    }

}
