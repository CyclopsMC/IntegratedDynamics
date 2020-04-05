package org.cyclops.integrateddynamics.core.recipe.type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.RecipeSerializerHelpers;

import javax.annotation.Nullable;

/**
 * Recipe serializer for drying basin recipes
 * @author rubensworks
 */
public class RecipeSerializerDryingBasin extends ForgeRegistryEntry<IRecipeSerializer<?>>
        implements IRecipeSerializer<RecipeDryingBasin> {

    @Override
    public RecipeDryingBasin read(ResourceLocation recipeId, JsonObject json) {
        JsonObject result = JSONUtils.getJsonObject(json, "result");

        // Input
        Ingredient inputIngredient = RecipeSerializerHelpers.getJsonIngredient(json, "item", false);
        FluidStack inputFluid = RecipeSerializerHelpers.getJsonFluidStack(json, "fluid", false);

        // Output
        ItemStack outputItemStack = RecipeSerializerHelpers.getJsonItemStack(result, "item", false);
        FluidStack outputFluid = RecipeSerializerHelpers.getJsonFluidStack(result, "fluid", false);

        // Other stuff
        int duration = JSONUtils.getInt(json, "duration");

        // Validation
        if (inputIngredient.hasNoMatchingItems() && inputFluid.isEmpty()) {
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

        return new RecipeDryingBasin(recipeId, inputIngredient, inputFluid, outputItemStack, outputFluid, duration);
    }

    @Nullable
    @Override
    public RecipeDryingBasin read(ResourceLocation recipeId, PacketBuffer buffer) {
        // Input
        Ingredient inputIngredient = Ingredient.read(buffer);
        FluidStack inputFluid = FluidStack.readFromPacket(buffer);

        // Output
        ItemStack outputItemStack = buffer.readItemStack();
        FluidStack outputFluid = FluidStack.readFromPacket(buffer);

        // Other stuff
        int duration = buffer.readVarInt();

        return new RecipeDryingBasin(recipeId, inputIngredient, inputFluid, outputItemStack, outputFluid, duration);
    }

    @Override
    public void write(PacketBuffer buffer, RecipeDryingBasin recipe) {
        // Input
        recipe.getInputIngredient().write(buffer);
        recipe.getInputFluid().writeToPacket(buffer);

        // Output
        buffer.writeItemStack(recipe.getOutputItem());
        recipe.getOutputFluid().writeToPacket(buffer);

        // Other stuff
        buffer.writeVarInt(recipe.getDuration());
    }
}
