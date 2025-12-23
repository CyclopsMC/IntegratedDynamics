package org.cyclops.integrateddynamics.core.recipe.type;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

public class RecipeSerializerSqueezerFacade implements RecipeSerializer<RecipeSqueezerFacade> {

    @Override
    public RecipeSqueezerFacade fromJson(ResourceLocation recipeId, JsonObject json) {
        return new RecipeSqueezerFacade(recipeId, Ingredient.of(org.cyclops.integrateddynamics.RegistryEntries.ITEM_FACADE));
    }

    @Override
    public @Nullable RecipeSqueezerFacade fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        return new RecipeSqueezerFacade(recipeId, Ingredient.of(org.cyclops.integrateddynamics.RegistryEntries.ITEM_FACADE));
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, RecipeSqueezerFacade recipe) { }

}
