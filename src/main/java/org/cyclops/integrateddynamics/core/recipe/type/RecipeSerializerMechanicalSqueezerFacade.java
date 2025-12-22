package org.cyclops.integrateddynamics.core.recipe.type;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;

public class RecipeSerializerMechanicalSqueezerFacade implements RecipeSerializer<RecipeMechanicalSqueezerFacade> {

    @Override
    public RecipeMechanicalSqueezerFacade fromJson(ResourceLocation recipeId, JsonObject json) {

        //Duration
        int duration = GsonHelper.getAsInt(json, "duration");

        return new RecipeMechanicalSqueezerFacade(recipeId, Ingredient.of(org.cyclops.integrateddynamics.RegistryEntries.ITEM_FACADE), null, FluidStack.EMPTY, duration);
    }

    @Override
    public @Nullable RecipeMechanicalSqueezerFacade fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {

        // Output
        int duration = buffer.readVarInt();

        return new RecipeMechanicalSqueezerFacade(recipeId, Ingredient.of(org.cyclops.integrateddynamics.RegistryEntries.ITEM_FACADE), null, FluidStack.EMPTY, duration);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, RecipeMechanicalSqueezerFacade recipe) {

        // Duration
        buffer.writeVarInt(recipe.getDuration());
    }

}
