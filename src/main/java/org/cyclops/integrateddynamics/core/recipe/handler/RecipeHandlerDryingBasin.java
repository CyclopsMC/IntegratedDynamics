package org.cyclops.integrateddynamics.core.recipe.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.cyclops.commoncapabilities.api.capability.fluidhandler.FluidMatch;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IPrototypedIngredientAlternatives;
import org.cyclops.commoncapabilities.api.capability.recipehandler.PrototypedIngredientAlternativesList;
import org.cyclops.commoncapabilities.api.ingredient.IMixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.MixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;
import org.cyclops.cyclopscore.ingredient.recipe.IngredientRecipeHelpers;
import org.cyclops.cyclopscore.ingredient.recipe.RecipeHandlerRecipeType;
import org.cyclops.cyclopscore.recipe.type.IInventoryFluid;
import org.cyclops.cyclopscore.recipe.type.InventoryFluid;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeDryingBasin;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author rubensworks
 */
public class RecipeHandlerDryingBasin<T extends RecipeDryingBasin> extends RecipeHandlerRecipeType<IInventoryFluid, T> {

    public RecipeHandlerDryingBasin(Supplier<Level> worldSupplier, RecipeType<T> recipeType) {
        super(worldSupplier,
                recipeType,
                Sets.newHashSet(IngredientComponent.ITEMSTACK, IngredientComponent.FLUIDSTACK),
                Sets.newHashSet(IngredientComponent.ITEMSTACK, IngredientComponent.FLUIDSTACK));
    }

    @Nullable
    @Override
    protected IInventoryFluid getRecipeInputContainer(IMixedIngredients input) {
        InventoryFluid inventory = new InventoryFluid(NonNullList.withSize(1, ItemStack.EMPTY), NonNullList.withSize(1, FluidStack.EMPTY));
        if (!input.getInstances(IngredientComponent.ITEMSTACK).isEmpty()) {
            inventory.setItem(0, input.getInstances(IngredientComponent.ITEMSTACK).get(0));
        }
        if (!input.getInstances(IngredientComponent.FLUIDSTACK).isEmpty()) {
            inventory.getFluidHandler().fill(input.getInstances(IngredientComponent.FLUIDSTACK).get(0), IFluidHandler.FluidAction.EXECUTE);
        }
        return inventory;
    }

    @Nullable
    @Override
    protected Map<IngredientComponent<?, ?>, List<IPrototypedIngredientAlternatives<?, ?>>> getRecipeInputIngredients(RecipeDryingBasin recipe) {
        Map<IngredientComponent<?, ?>, List<IPrototypedIngredientAlternatives<?, ?>>> inputs = Maps.newIdentityHashMap();
        if (recipe.getInputIngredient().isPresent()) {
            inputs.put(IngredientComponent.ITEMSTACK, Lists.newArrayList(IngredientRecipeHelpers.getPrototypesFromIngredient(recipe.getInputIngredient().get())));
        }
        if (recipe.getInputFluid().isPresent()) {
            inputs.put(IngredientComponent.FLUIDSTACK, Lists.newArrayList(new PrototypedIngredientAlternativesList<>(
                    Lists.newArrayList(new PrototypedIngredient<>(IngredientComponent.FLUIDSTACK, recipe.getInputFluid().get(), FluidMatch.EXACT)))));
        }
        return inputs;
    }

    @Nullable
    @Override
    protected IMixedIngredients getRecipeOutputIngredients(RecipeDryingBasin recipe) {
        Map<IngredientComponent<?, ?>, List<?>> outputIngredients = Maps.newIdentityHashMap();
        if (!recipe.getOutputItemFirst().isEmpty()) {
            outputIngredients.put(IngredientComponent.ITEMSTACK, Lists.newArrayList(recipe.getOutputItemFirst()));
        }
        if (!recipe.getOutputFluid().isEmpty()) {
            outputIngredients.put(IngredientComponent.FLUIDSTACK, Lists.newArrayList(recipe.getOutputFluid().get()));
        }

        // Validate output
        if (outputIngredients.isEmpty()) {
            return null;
        }

        return new MixedIngredients(outputIngredients);
    }

    @Override
    public boolean isValidSizeInput(IngredientComponent<?, ?> ingredientComponent, int size) {
        return (ingredientComponent == IngredientComponent.ITEMSTACK || ingredientComponent == IngredientComponent.FLUIDSTACK)
                && size == 1;
    }
}
