package org.cyclops.integrateddynamics.core.recipe.custom;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import net.minecraft.item.ItemStack;
import org.cyclops.commoncapabilities.api.ingredient.IMixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.recipe.custom.RecipeHandlerMachine;
import org.cyclops.cyclopscore.recipe.custom.api.IMachine;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientAndFluidStackRecipeComponent;

/**
 * @author rubensworks
 */
public class RecipeHandlerDryingBasin<M extends IMachine<M, IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, P>,
        P extends IRecipeProperties> extends RecipeHandlerMachine<M, IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, P> {

    public RecipeHandlerDryingBasin(M machine) {
        super(machine,
                Sets.newHashSet(IngredientComponent.ITEMSTACK),
                Sets.newHashSet(IngredientComponent.ITEMSTACK, IngredientComponent.FLUIDSTACK));
    }

    @Override
    public boolean isValidSizeInput(IngredientComponent<?, ?> component, int size) {
        return (component == IngredientComponent.ITEMSTACK || component == IngredientComponent.FLUIDSTACK) && size == 1;
    }

    @Override
    protected IngredientAndFluidStackRecipeComponent inputIngredientsToRecipeInput(IMixedIngredients inputIngredients) {
        return new IngredientAndFluidStackRecipeComponent(
                Iterables.getFirst(inputIngredients.getInstances(IngredientComponent.ITEMSTACK), ItemStack.EMPTY),
                Iterables.getFirst(inputIngredients.getInstances(IngredientComponent.FLUIDSTACK), null)
        );
    }
}
