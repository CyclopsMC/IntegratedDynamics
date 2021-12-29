package org.cyclops.integrateddynamics.core.recipe.type;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.cyclops.cyclopscore.helper.RecipeSerializerHelpers;

import javax.annotation.Nullable;

/**
 * Recipe serializer for NBT clear recipes.
 * @author rubensworks
 */
public class RecipeSerializerNbtClear extends ForgeRegistryEntry<RecipeSerializer<?>>
        implements RecipeSerializer<RecipeNbtClear> {

    @Override
    public RecipeNbtClear fromJson(ResourceLocation recipeId, JsonObject json) {
        Ingredient inputIngredient = RecipeSerializerHelpers.getJsonIngredient(json, "item", false);
        return new RecipeNbtClear(recipeId, inputIngredient);
    }

    @Nullable
    @Override
    public RecipeNbtClear fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        Ingredient inputIngredient = Ingredient.fromNetwork(buffer);
        return new RecipeNbtClear(recipeId, inputIngredient);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, RecipeNbtClear recipe) {
        recipe.getInputIngredient().toNetwork(buffer);
    }
}
