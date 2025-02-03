package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.helper.RecipeSerializerHelpers;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;

public class RecipeSerializerFacadeSqueezeMechanical implements RecipeSerializer<RecipeFacadeSqueezeMechanical> {

    @Override
    public RecipeFacadeSqueezeMechanical fromJson(ResourceLocation recipeId, JsonObject json) {
        JsonObject result = GsonHelper.getAsJsonObject(json, "result");

        // Input
        Ingredient inputIngredient = RecipeSerializerHelpers.getJsonIngredient(json, "item", true);

        // Output
        NonNullList<RecipeSqueezer.IngredientChance> outputItemStacks = RecipeSerializerSqueezer.getJsonItemStackChances(result, "items");
        FluidStack outputFluid = RecipeSerializerHelpers.getJsonFluidStack(result, "fluid", false);

        //Duration
        int duration = GsonHelper.getAsInt(json, "duration");

        return new RecipeFacadeSqueezeMechanical(recipeId, inputIngredient, outputItemStacks, outputFluid, duration);
    }

    @Override
    public @Nullable RecipeFacadeSqueezeMechanical fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        // Input
        Ingredient inputIngredient = Ingredient.fromNetwork(buffer);

        // Output
        NonNullList<RecipeSqueezer.IngredientChance> outputItemStacks = NonNullList.create();
        int outputItemStacksCount = buffer.readInt();
        for (int i = 0; i < outputItemStacksCount; i++) {
            outputItemStacks.add(new RecipeSqueezer.IngredientChance(
                    RecipeSerializerHelpers.readItemStackOrItemStackIngredient(buffer),
                    buffer.readFloat()
            ));
        }

        int duration = buffer.readVarInt();

        return new RecipeFacadeSqueezeMechanical(recipeId, inputIngredient, outputItemStacks, FluidStack.EMPTY, duration);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, RecipeFacadeSqueezeMechanical recipe) {
        // Input
        recipe.getInputIngredient().toNetwork(buffer);

        // Output
        buffer.writeInt(recipe.getOutputItems().size());
        for (RecipeSqueezer.IngredientChance outputItem : recipe.getOutputItems()) {
            RecipeSerializerHelpers.writeItemStackOrItemStackIngredient(buffer, outputItem.getIngredient());
            buffer.writeFloat(outputItem.getChance());
        }

        // Duration
        buffer.writeVarInt(recipe.getDuration());
    }

}
