package org.cyclops.integrateddynamics.core.evaluate.variable.recipe;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;

import java.util.List;
import java.util.function.Predicate;

/**
 * An implementation of {@link IIngredients} by storing lists of lists for each ingredient type.
 *
 * The goal of this interface is only to provide a list-based basis for ingredients.
 * No custom predicates are supported as the implementation only checks for containment in lists.
 *
 * @author rubensworks
 */
public class IngredientsRecipeLists implements IIngredients {

    private final List<List<ValueObjectTypeItemStack.ValueItemStack>> itemStacks;
    private final List<List<ValueObjectTypeFluidStack.ValueFluidStack>> fluidStacks;
    private final List<List<ValueTypeInteger.ValueInteger>> energies;

    public IngredientsRecipeLists(List<List<ValueObjectTypeItemStack.ValueItemStack>> itemStacks,
                                  List<List<ValueObjectTypeFluidStack.ValueFluidStack>> fluidStacks,
                                  List<List<ValueTypeInteger.ValueInteger>> energies) {
        this.itemStacks = itemStacks;
        this.fluidStacks = fluidStacks;
        this.energies = energies;
    }

    @Override
    public int getItemStackIngredients() {
        return itemStacks.size();
    }

    @Override
    public List<ValueObjectTypeItemStack.ValueItemStack> getItemStacks(int index) {
        return itemStacks.get(index);
    }

    @Override
    public Predicate<ValueObjectTypeItemStack.ValueItemStack> getItemStackPredicate(int index) {
        return itemStacks.get(index)::contains;
    }

    @Override
    public List<List<ValueObjectTypeItemStack.ValueItemStack>> getItemStacksRaw() {
        return itemStacks;
    }

    @Override
    public int getFluidStackIngredients() {
        return fluidStacks.size();
    }

    @Override
    public List<ValueObjectTypeFluidStack.ValueFluidStack> getFluidStacks(int index) {
        return fluidStacks.get(index);
    }

    @Override
    public Predicate<ValueObjectTypeFluidStack.ValueFluidStack> getFluidStackPredicate(int index) {
        return fluidStacks.get(index)::contains;
    }

    @Override
    public List<List<ValueObjectTypeFluidStack.ValueFluidStack>> getFluidStacksRaw() {
        return fluidStacks;
    }

    @Override
    public int getEnergyIngredients() {
        return energies.size();
    }

    @Override
    public List<ValueTypeInteger.ValueInteger> getEnergies(int index) {
        return energies.get(index);
    }

    @Override
    public Predicate<ValueTypeInteger.ValueInteger> getEnergiesPredicate(int index) {
        return energies.get(index)::contains;
    }

    @Override
    public List<List<ValueTypeInteger.ValueInteger>> getEnergiesRaw() {
        return energies;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof IIngredients
                && this.getItemStacksRaw().equals(((IIngredients) obj).getItemStacksRaw())
                && this.getFluidStacksRaw().equals(((IIngredients) obj).getFluidStacksRaw())
                && this.getEnergiesRaw().equals(((IIngredients) obj).getEnergiesRaw()));
    }

    @Override
    public String toString() {
        return "items: " + getItemStacksRaw() + "; fluids: " + getFluidStacksRaw() + "; energies: " + getEnergiesRaw();
    }
}
