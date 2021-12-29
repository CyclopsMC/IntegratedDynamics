package org.cyclops.integrateddynamics.core.recipe.type;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.cyclops.cyclopscore.helper.RecipeSerializerHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;

import javax.annotation.Nullable;

/**
 * Recipe serializer for mechanical drying basin recipes
 * @author rubensworks
 */
public class RecipeSerializerMechanicalDryingBasin extends ForgeRegistryEntry<RecipeSerializer<?>>
        implements RecipeSerializer<RecipeMechanicalDryingBasin> {

    @Override
    public RecipeMechanicalDryingBasin fromJson(ResourceLocation recipeId, JsonObject json) {
        JsonObject result = GsonHelper.getAsJsonObject(json, "result");

        // Input
        Ingredient inputIngredient = RecipeSerializerHelpers.getJsonIngredient(json, "item", false);
        FluidStack inputFluid = RecipeSerializerHelpers.getJsonFluidStack(json, "fluid", false);

        // Output
        ItemStack outputItemStack = RecipeSerializerHelpers.getJsonItemStackOrTag(result, false, GeneralConfig.recipeTagOutputModPriorities);
        FluidStack outputFluid = RecipeSerializerHelpers.getJsonFluidStack(result, "fluid", false);

        // Other stuff
        int duration = GsonHelper.getAsInt(json, "duration");

        // Validation
        if (inputIngredient.isEmpty() && inputFluid.isEmpty()) {
            throw new JsonSyntaxException("An input item or fluid is required");
        }
        if (outputItemStack.isEmpty() && outputFluid.isEmpty()) {
            throw new JsonSyntaxException("An output item or fluid is required");
        }
        if (!inputFluid.isEmpty() && !outputFluid.isEmpty()) {
            throw new JsonSyntaxException("Can't have both an input and output fluid");
        }
        if (duration <= 0) {
            throw new JsonSyntaxException("Durations must be higher than one tick");
        }

        return new RecipeMechanicalDryingBasin(recipeId, inputIngredient, inputFluid, outputItemStack, outputFluid, duration);
    }

    @Nullable
    @Override
    public RecipeMechanicalDryingBasin fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        // Input
        Ingredient inputIngredient = Ingredient.fromNetwork(buffer);
        FluidStack inputFluid = FluidStack.readFromPacket(buffer);

        // Output
        ItemStack outputItemStack = buffer.readItem();
        FluidStack outputFluid = FluidStack.readFromPacket(buffer);

        // Other stuff
        int duration = buffer.readVarInt();

        return new RecipeMechanicalDryingBasin(recipeId, inputIngredient, inputFluid, outputItemStack, outputFluid, duration);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, RecipeMechanicalDryingBasin recipe) {
        // Input
        recipe.getInputIngredient().toNetwork(buffer);
        recipe.getInputFluid().writeToPacket(buffer);

        // Output
        buffer.writeItem(recipe.getOutputItem());
        recipe.getOutputFluid().writeToPacket(buffer);

        // Other stuff
        buffer.writeVarInt(recipe.getDuration());
    }
}
