package org.cyclops.integrateddynamics.core.recipe.type;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class RecipeSerializerFacadeSqueezeMechanical implements RecipeSerializer<RecipeFacadeSqueezeMechanical> {

    @Override
    public RecipeFacadeSqueezeMechanical fromJson(ResourceLocation recipeId, JsonObject json) {

        //Duration
        int duration = GsonHelper.getAsInt(json, "duration");

        return new RecipeFacadeSqueezeMechanical(recipeId, Ingredient.of(ForgeRegistries.ITEMS.getValue(new ResourceLocation("integrateddynamics", "facade"))), null, FluidStack.EMPTY, duration);
    }

    @Override
    public @Nullable RecipeFacadeSqueezeMechanical fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {

        // Output
        int duration = buffer.readVarInt();

        return new RecipeFacadeSqueezeMechanical(recipeId, Ingredient.of(ForgeRegistries.ITEMS.getValue(new ResourceLocation("integrateddynamics", "facade"))), null, FluidStack.EMPTY, duration);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, RecipeFacadeSqueezeMechanical recipe) {

        // Duration
        buffer.writeVarInt(recipe.getDuration());
    }

}
