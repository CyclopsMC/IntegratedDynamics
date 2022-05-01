package org.cyclops.integrateddynamics.core.recipe.type;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.cyclops.cyclopscore.helper.RecipeSerializerHelpers;
import org.cyclops.cyclopscore.recipe.ItemStackFromIngredient;
import org.cyclops.integrateddynamics.GeneralConfig;

import javax.annotation.Nullable;

/**
 * Recipe serializer for squeezer recipes
 * @author rubensworks
 */
public class RecipeSerializerSqueezer extends ForgeRegistryEntry<RecipeSerializer<?>>
        implements RecipeSerializer<RecipeSqueezer> {

    protected static RecipeSqueezer.IngredientChance getJsonItemStackChance(JsonObject json) {
        Either<ItemStack, ItemStackFromIngredient> itemStack = RecipeSerializerHelpers.getJsonItemStackOrTag(json, true, GeneralConfig.recipeTagOutputModPriorities);
        float chance = GsonHelper.getAsFloat(json, "chance", 1.0F);
        return new RecipeSqueezer.IngredientChance(itemStack, chance);
    }

    protected static NonNullList<RecipeSqueezer.IngredientChance> getJsonItemStackChances(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null) {
            return NonNullList.create();
        } else if (element.isJsonArray()) {
            JsonArray jsonElements = element.getAsJsonArray();
            NonNullList<RecipeSqueezer.IngredientChance> elements = NonNullList.create();
            for (JsonElement jsonElement : jsonElements) {
                elements.add(getJsonItemStackChance(jsonElement.getAsJsonObject()));
            }
            return elements;
        } else {
            throw new JsonSyntaxException("A JSON array is required as value for " + key);
        }
    }

    @Override
    public RecipeSqueezer fromJson(ResourceLocation recipeId, JsonObject json) {
        JsonObject result = GsonHelper.getAsJsonObject(json, "result");

        // Input
        Ingredient inputIngredient = RecipeSerializerHelpers.getJsonIngredient(json, "item", true);

        // Output
        NonNullList<RecipeSqueezer.IngredientChance> outputItemStacks = getJsonItemStackChances(result, "items");
        FluidStack outputFluid = RecipeSerializerHelpers.getJsonFluidStack(result, "fluid", false);

        // Validation
        if (inputIngredient.isEmpty()) {
            throw new JsonSyntaxException("An input item is required");
        }
        if (outputItemStacks.isEmpty() && outputFluid.isEmpty()) {
            throw new JsonSyntaxException("An output item or fluid is required");
        }

        return new RecipeSqueezer(recipeId, inputIngredient, outputItemStacks, outputFluid);
    }

    @Nullable
    @Override
    public RecipeSqueezer fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
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

        return new RecipeSqueezer(recipeId, inputIngredient, outputItemStacks, outputFluid);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, RecipeSqueezer recipe) {
        // Input
        recipe.getInputIngredient().toNetwork(buffer);

        // Output
        buffer.writeInt(recipe.getOutputItems().size());
        for (RecipeSqueezer.IngredientChance outputItem : recipe.getOutputItems()) {
            RecipeSerializerHelpers.writeItemStackOrItemStackIngredient(buffer, outputItem.getIngredient());
            recipe.getOutputFluid().writeToPacket(buffer);

            buffer.writeFloat(outputItem.getChance());
        }
        recipe.getOutputFluid().writeToPacket(buffer);
    }
}
