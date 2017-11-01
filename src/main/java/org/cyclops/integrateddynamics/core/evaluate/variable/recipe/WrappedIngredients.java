package org.cyclops.integrateddynamics.core.evaluate.variable.recipe;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;

import java.util.List;
import java.util.function.Predicate;

/**
 * A wrapper around ingredients.
 * @author rubensworks
 */
public class WrappedIngredients implements IIngredients {

    private final IIngredients ingredients;

    public WrappedIngredients(IIngredients ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public int getItemStackIngredients() {
        return ingredients.getItemStackIngredients();
    }

    @Override
    public List<ValueObjectTypeItemStack.ValueItemStack> getItemStacks(int index) {
        return ingredients.getItemStacks(index);
    }

    @Override
    public Predicate<ValueObjectTypeItemStack.ValueItemStack> getItemStackPredicate(int index) {
        return ingredients.getItemStackPredicate(index);
    }

    @Override
    public List<List<ValueObjectTypeItemStack.ValueItemStack>> getItemStacksRaw() {
        return ingredients.getItemStacksRaw();
    }

    @Override
    public int getFluidStackIngredients() {
        return ingredients.getFluidStackIngredients();
    }

    @Override
    public List<ValueObjectTypeFluidStack.ValueFluidStack> getFluidStacks(int index) {
        return ingredients.getFluidStacks(index);
    }

    @Override
    public Predicate<ValueObjectTypeFluidStack.ValueFluidStack> getFluidStackPredicate(int index) {
        return ingredients.getFluidStackPredicate(index);
    }

    @Override
    public List<List<ValueObjectTypeFluidStack.ValueFluidStack>> getFluidStacksRaw() {
        return ingredients.getFluidStacksRaw();
    }

    @Override
    public int getEnergyIngredients() {
        return ingredients.getEnergyIngredients();
    }

    @Override
    public List<ValueTypeInteger.ValueInteger> getEnergies(int index) {
        return ingredients.getEnergies(index);
    }

    @Override
    public Predicate<ValueTypeInteger.ValueInteger> getEnergiesPredicate(int index) {
        return ingredients.getEnergiesPredicate(index);
    }

    @Override
    public List<List<ValueTypeInteger.ValueInteger>> getEnergiesRaw() {
        return ingredients.getEnergiesRaw();
    }
}
