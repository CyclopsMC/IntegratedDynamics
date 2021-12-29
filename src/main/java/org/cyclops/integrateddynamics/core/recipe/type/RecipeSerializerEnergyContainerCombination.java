package org.cyclops.integrateddynamics.core.recipe.type;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.cyclops.cyclopscore.helper.RecipeSerializerHelpers;

import javax.annotation.Nullable;

/**
 * Recipe serializer for energy container combinations.
 * @author rubensworks
 */
public class RecipeSerializerEnergyContainerCombination extends ForgeRegistryEntry<RecipeSerializer<?>>
        implements RecipeSerializer<RecipeEnergyContainerCombination> {

    @Override
    public RecipeEnergyContainerCombination fromJson(ResourceLocation recipeId, JsonObject json) {
        Ingredient inputIngredient = RecipeSerializerHelpers.getJsonIngredient(json, "item", false);
        int maxCapacity = GsonHelper.getAsInt(json, "maxCapacity");
        return new RecipeEnergyContainerCombination(recipeId, inputIngredient, maxCapacity);
    }

    @Nullable
    @Override
    public RecipeEnergyContainerCombination fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        Ingredient inputIngredient = Ingredient.fromNetwork(buffer);
        int maxCapacity = buffer.readInt();
        return new RecipeEnergyContainerCombination(recipeId, inputIngredient, maxCapacity);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, RecipeEnergyContainerCombination recipe) {
        recipe.getBatteryItem().toNetwork(buffer);
        buffer.writeInt(recipe.getMaxCapacity());
    }
}
