package org.cyclops.integrateddynamics.core.recipe.type;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

public class RecipeSerializerMechanicalSqueezerFacade implements RecipeSerializer<RecipeMechanicalSqueezerFacade> {

    @Override
    public RecipeMechanicalSqueezerFacade fromJson(ResourceLocation recipeId, JsonObject json) {
        int duration = GsonHelper.getAsInt(json, "duration");
        return new RecipeMechanicalSqueezerFacade(recipeId, Ingredient.of(org.cyclops.integrateddynamics.RegistryEntries.ITEM_FACADE), duration);
    }

    @Override
    public @Nullable RecipeMechanicalSqueezerFacade fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        int duration = buffer.readVarInt();
        return new RecipeMechanicalSqueezerFacade(recipeId, Ingredient.of(org.cyclops.integrateddynamics.RegistryEntries.ITEM_FACADE), duration);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, RecipeMechanicalSqueezerFacade recipe) {
        buffer.writeVarInt(recipe.getDuration());
    }

}
