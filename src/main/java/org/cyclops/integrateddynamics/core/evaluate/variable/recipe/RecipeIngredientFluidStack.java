package org.cyclops.integrateddynamics.core.evaluate.variable.recipe;

import com.google.common.collect.Lists;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.commoncapabilities.api.capability.recipehandler.FluidHandlerRecipeTarget;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeIngredient;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeComponent;

import java.util.List;

/**
 * A recipe ingredient implementation for {@link FluidStack}.
 * @author rubensworks
 */
public class RecipeIngredientFluidStack implements IRecipeIngredient<FluidStack, FluidHandlerRecipeTarget> {

    private final List<FluidStack> fluidStacks;

    public RecipeIngredientFluidStack(List<FluidStack> fluidStacks) {
        this.fluidStacks = fluidStacks;
    }

    public RecipeIngredientFluidStack(FluidStack... fluidStacks) {
        this(Lists.newArrayList(fluidStacks));
    }

    @Override
    public RecipeComponent<FluidStack, FluidHandlerRecipeTarget> getComponent() {
        return RecipeComponent.FLUIDSTACK;
    }

    @Override
    public List<FluidStack> getMatchingInstances() {
        return this.fluidStacks;
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        for (FluidStack stack : fluidStacks) {
            if (fluidStack.isFluidStackIdentical(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "[RecipeIngredientFluidStack ingredient: " + getMatchingInstances() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IRecipeIngredient
                && this.getComponent() == ((IRecipeIngredient) obj).getComponent()
                && this.getMatchingInstances().equals(((IRecipeIngredient) obj).getMatchingInstances());
    }
}
