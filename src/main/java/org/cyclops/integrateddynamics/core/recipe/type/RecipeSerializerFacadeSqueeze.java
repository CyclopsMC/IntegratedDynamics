package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.integrateddynamics.item.ItemFacade;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;

public class RecipeSerializerFacadeSqueeze implements RecipeSerializer<RecipeFacadeSqueeze> {

    @Override
    public RecipeFacadeSqueeze fromJson(ResourceLocation recipeId, JsonObject json) {
        return new RecipeFacadeSqueeze(recipeId, Ingredient.of(new ItemFacade(null)), null, FluidStack.EMPTY);
    }

    @Override
    public @Nullable RecipeFacadeSqueeze fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        return new RecipeFacadeSqueeze(recipeId, Ingredient.of(new ItemFacade(null)), null, FluidStack.EMPTY);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, RecipeFacadeSqueeze recipe) { }

}
