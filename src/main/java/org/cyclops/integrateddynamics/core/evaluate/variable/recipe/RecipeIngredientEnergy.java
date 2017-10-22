package org.cyclops.integrateddynamics.core.evaluate.variable.recipe;

import com.google.common.collect.Lists;
import net.minecraftforge.energy.IEnergyStorage;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeIngredient;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeComponent;

import java.util.List;
import java.util.Objects;

/**
 * A recipe ingredient implementation for energy.
 * @author rubensworks
 */
public class RecipeIngredientEnergy implements IRecipeIngredient<Integer, IEnergyStorage> {

    private final List<Integer> energies;

    public RecipeIngredientEnergy(List<Integer> energies) {
        this.energies = energies;
    }

    public RecipeIngredientEnergy(Integer... energies) {
        this(Lists.newArrayList(energies));
    }

    @Override
    public RecipeComponent<Integer, IEnergyStorage> getComponent() {
        return RecipeComponent.ENERGY;
    }

    @Override
    public List<Integer> getMatchingInstances() {
        return this.energies;
    }

    @Override
    public boolean test(Integer energy) {
        for (Integer energyThis : energies) {
            if (Objects.equals(energyThis, energy)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "[RecipeIngredientEnergy ingredient: " + getMatchingInstances() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IRecipeIngredient
                && this.getComponent() == ((IRecipeIngredient) obj).getComponent()
                && this.getMatchingInstances().equals(((IRecipeIngredient) obj).getMatchingInstances());
    }
}
