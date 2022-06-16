package org.cyclops.integrateddynamics.core.recipe.type;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.helper.RecipeSerializerHelpers;

import javax.annotation.Nullable;

/**
 * Recipe serializer for mechanical squeezer recipes
 * @author rubensworks
 */
public class RecipeSerializerMechanicalSqueezer implements RecipeSerializer<RecipeMechanicalSqueezer> {

    @Override
    public RecipeMechanicalSqueezer fromJson(ResourceLocation recipeId, JsonObject json) {
        JsonObject result = GsonHelper.getAsJsonObject(json, "result");

        // Input
        Ingredient inputIngredient = RecipeSerializerHelpers.getJsonIngredient(json, "item", true);

        // Output
        NonNullList<RecipeSqueezer.IngredientChance> outputItemStacks = RecipeSerializerSqueezer.getJsonItemStackChances(result, "items");
        FluidStack outputFluid = RecipeSerializerHelpers.getJsonFluidStack(result, "fluid", false);

        // Other stuff
        int duration = GsonHelper.getAsInt(json, "duration");

        // Validation
        if (inputIngredient.isEmpty()) {
            throw new JsonSyntaxException("An input item is required");
        }
        if (outputItemStacks.isEmpty() && outputFluid.isEmpty()) {
            throw new JsonSyntaxException("An output item or fluid is required");
        }
        if (duration <= 0) {
            throw new JsonSyntaxException("Durations must be higher than one tick");
        }

        return new RecipeMechanicalSqueezer(recipeId, inputIngredient, outputItemStacks, outputFluid, duration);
    }

    @Nullable
    @Override
    public RecipeMechanicalSqueezer fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
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
        FluidStack outputFluid = FluidStack.readFromPacket(buffer);

        // Other stuff
        int duration = buffer.readVarInt();

        return new RecipeMechanicalSqueezer(recipeId, inputIngredient, outputItemStacks, outputFluid, duration);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, RecipeMechanicalSqueezer recipe) {
        // Input
        recipe.getInputIngredient().toNetwork(buffer);

        // Output
        buffer.writeInt(recipe.getOutputItems().size());
        for (RecipeSqueezer.IngredientChance outputItem : recipe.getOutputItems()) {
            RecipeSerializerHelpers.writeItemStackOrItemStackIngredient(buffer, outputItem.getIngredient());
            buffer.writeFloat(outputItem.getChance());
        }
        recipe.getOutputFluid().writeToPacket(buffer);

        // Other stuff
        buffer.writeVarInt(recipe.getDuration());
    }
}
