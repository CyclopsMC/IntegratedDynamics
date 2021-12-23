package org.cyclops.integrateddynamics.core.recipe.type;

import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.cyclops.cyclopscore.helper.RecipeSerializerHelpers;

import javax.annotation.Nullable;

/**
 * Recipe serializer for energy container combinations.
 * @author rubensworks
 */
public class RecipeSerializerEnergyContainerCombination extends ForgeRegistryEntry<IRecipeSerializer<?>>
        implements IRecipeSerializer<RecipeEnergyContainerCombination> {

    @Override
    public RecipeEnergyContainerCombination fromJson(ResourceLocation recipeId, JsonObject json) {
        Ingredient inputIngredient = RecipeSerializerHelpers.getJsonIngredient(json, "item", false);
        int maxCapacity = JSONUtils.getAsInt(json, "maxCapacity");
        return new RecipeEnergyContainerCombination(recipeId, inputIngredient, maxCapacity);
    }

    @Nullable
    @Override
    public RecipeEnergyContainerCombination fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
        Ingredient inputIngredient = Ingredient.fromNetwork(buffer);
        int maxCapacity = buffer.readInt();
        return new RecipeEnergyContainerCombination(recipeId, inputIngredient, maxCapacity);
    }

    @Override
    public void toNetwork(PacketBuffer buffer, RecipeEnergyContainerCombination recipe) {
        recipe.getBatteryItem().toNetwork(buffer);
        buffer.writeInt(recipe.getMaxCapacity());
    }
}
