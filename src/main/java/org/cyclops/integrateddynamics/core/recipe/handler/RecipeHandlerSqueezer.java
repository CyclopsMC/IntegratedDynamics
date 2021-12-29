package org.cyclops.integrateddynamics.core.recipe.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IPrototypedIngredientAlternatives;
import org.cyclops.commoncapabilities.api.ingredient.IMixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.MixedIngredients;
import org.cyclops.cyclopscore.ingredient.recipe.IngredientRecipeHelpers;
import org.cyclops.cyclopscore.ingredient.recipe.RecipeHandlerRecipeType;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeSqueezer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author rubensworks
 */
public class RecipeHandlerSqueezer extends RecipeHandlerRecipeType<Container, RecipeSqueezer> {

    public RecipeHandlerSqueezer(Supplier<Level> worldSupplier) {
        super(worldSupplier,
                RegistryEntries.RECIPETYPE_SQUEEZER,
                Sets.newHashSet(IngredientComponent.ITEMSTACK),
                Sets.newHashSet(IngredientComponent.ITEMSTACK, IngredientComponent.FLUIDSTACK));
    }

    @Nullable
    @Override
    protected Container getRecipeInputContainer(IMixedIngredients input) {
        Container inventory = new SimpleContainer(1);
        inventory.setItem(0, input.getInstances(IngredientComponent.ITEMSTACK).get(0));
        return inventory;
    }

    @Nullable
    @Override
    protected Map<IngredientComponent<?, ?>, List<IPrototypedIngredientAlternatives<?, ?>>> getRecipeInputIngredients(RecipeSqueezer recipe) {
        Map<IngredientComponent<?, ?>, List<IPrototypedIngredientAlternatives<?, ?>>> inputs = Maps.newIdentityHashMap();
        inputs.put(IngredientComponent.ITEMSTACK, Lists.newArrayList(IngredientRecipeHelpers.getPrototypesFromIngredient(recipe.getInputIngredient())));
        return inputs;
    }

    @Nullable
    @Override
    protected IMixedIngredients getRecipeOutputIngredients(RecipeSqueezer recipe) {
        Map<IngredientComponent<?, ?>, List<?>> outputIngredients = Maps.newIdentityHashMap();
        List<ItemStack> outputItems = recipe.getOutputItems().stream()
                .filter(i -> i.getChance() == 1.0F)
                .map(RecipeSqueezer.ItemStackChance::getItemStack)
                .collect(Collectors.toList());
        if (!outputItems.isEmpty()) {
            outputIngredients.put(IngredientComponent.ITEMSTACK, outputItems);
        }
        if (!recipe.getOutputFluid().isEmpty()) {
            outputIngredients.put(IngredientComponent.FLUIDSTACK, Lists.newArrayList(recipe.getOutputFluid()));
        }

        // Validate output
        if (outputIngredients.isEmpty()) {
            return null;
        }

        return new MixedIngredients(outputIngredients);
    }

    @Override
    public boolean isValidSizeInput(IngredientComponent<?, ?> ingredientComponent, int size) {
        return ingredientComponent == IngredientComponent.ITEMSTACK && size == 1;
    }
}
