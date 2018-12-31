package org.cyclops.integrateddynamics.core.recipe.custom;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import net.minecraft.item.ItemStack;
import org.cyclops.commoncapabilities.api.ingredient.IMixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.recipe.custom.RecipeHandlerMachine;
import org.cyclops.cyclopscore.recipe.custom.api.IMachine;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientRecipeComponent;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientsAndFluidStackRecipeComponent;

/**
 * @author rubensworks
 */
public class RecipeHandlerSqueezer<M extends IMachine<M, IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, P>,
        P extends IRecipeProperties> extends RecipeHandlerMachine<M, IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, P> {

    public RecipeHandlerSqueezer(M machine) {
        super(machine,
                Sets.newHashSet(IngredientComponent.ITEMSTACK),
                Sets.newHashSet(IngredientComponent.ITEMSTACK, IngredientComponent.FLUIDSTACK));
    }

    @Override
    public boolean isValidSizeInput(IngredientComponent<?, ?> component, int size) {
        return component == IngredientComponent.ITEMSTACK && size == 1;
    }

    @Override
    protected IngredientRecipeComponent inputIngredientsToRecipeInput(IMixedIngredients inputIngredients) {
        return new IngredientRecipeComponent(Iterables.getFirst(inputIngredients.getInstances(IngredientComponent.ITEMSTACK), ItemStack.EMPTY));
    }
}
