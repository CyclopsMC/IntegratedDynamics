package org.cyclops.integrateddynamics.core.recipe.type;

import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.cyclops.cyclopscore.helper.RecipeSerializerHelpers;

import javax.annotation.Nullable;

/**
 * Recipe serializer for NBT clear recipes.
 * @author rubensworks
 */
public class RecipeSerializerNbtClear extends ForgeRegistryEntry<IRecipeSerializer<?>>
        implements IRecipeSerializer<RecipeNbtClear> {

    @Override
    public RecipeNbtClear fromJson(ResourceLocation recipeId, JsonObject json) {
        Ingredient inputIngredient = RecipeSerializerHelpers.getJsonIngredient(json, "item", false);
        return new RecipeNbtClear(recipeId, inputIngredient);
    }

    @Nullable
    @Override
    public RecipeNbtClear fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
        Ingredient inputIngredient = Ingredient.fromNetwork(buffer);
        return new RecipeNbtClear(recipeId, inputIngredient);
    }

    @Override
    public void toNetwork(PacketBuffer buffer, RecipeNbtClear recipe) {
        recipe.getInputIngredient().toNetwork(buffer);
    }
}
