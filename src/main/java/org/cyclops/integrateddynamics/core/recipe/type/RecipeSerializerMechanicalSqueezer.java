package org.cyclops.integrateddynamics.core.recipe.type;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.cyclops.cyclopscore.helper.RecipeSerializerHelpers;

import javax.annotation.Nullable;

/**
 * Recipe serializer for mechanical squeezer recipes
 * @author rubensworks
 */
public class RecipeSerializerMechanicalSqueezer extends ForgeRegistryEntry<IRecipeSerializer<?>>
        implements IRecipeSerializer<RecipeMechanicalSqueezer> {

    @Override
    public RecipeMechanicalSqueezer fromJson(ResourceLocation recipeId, JsonObject json) {
        JsonObject result = JSONUtils.getAsJsonObject(json, "result");

        // Input
        Ingredient inputIngredient = RecipeSerializerHelpers.getJsonIngredient(json, "item", true);

        // Output
        NonNullList<RecipeSqueezer.ItemStackChance> outputItemStacks = RecipeSerializerSqueezer.getJsonItemStackChances(result, "items");
        FluidStack outputFluid = RecipeSerializerHelpers.getJsonFluidStack(result, "fluid", false);

        // Other stuff
        int duration = JSONUtils.getAsInt(json, "duration");

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
    public RecipeMechanicalSqueezer fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
        // Input
        Ingredient inputIngredient = Ingredient.fromNetwork(buffer);

        // Output
        NonNullList<RecipeSqueezer.ItemStackChance> outputItemStacks = NonNullList.create();
        int outputItemStacksCount = buffer.readInt();
        for (int i = 0; i < outputItemStacksCount; i++) {
            outputItemStacks.add(new RecipeSqueezer.ItemStackChance(
                    buffer.readItem(),
                    buffer.readFloat()
            ));
        }
        FluidStack outputFluid = FluidStack.readFromPacket(buffer);

        // Other stuff
        int duration = buffer.readVarInt();

        return new RecipeMechanicalSqueezer(recipeId, inputIngredient, outputItemStacks, outputFluid, duration);
    }

    @Override
    public void toNetwork(PacketBuffer buffer, RecipeMechanicalSqueezer recipe) {
        // Input
        recipe.getInputIngredient().toNetwork(buffer);

        // Output
        buffer.writeInt(recipe.getOutputItems().size());
        for (RecipeSqueezer.ItemStackChance outputItem : recipe.getOutputItems()) {
            buffer.writeItem(outputItem.getItemStack());
            buffer.writeFloat(outputItem.getChance());
        }
        recipe.getOutputFluid().writeToPacket(buffer);

        // Other stuff
        buffer.writeVarInt(recipe.getDuration());
    }
}
