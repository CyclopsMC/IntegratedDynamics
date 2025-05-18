package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.integrateddynamics.RegistryEntries;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;

public class RecipeFacadeSqueezeMechanical extends RecipeMechanicalSqueezer {

    public RecipeFacadeSqueezeMechanical(ResourceLocation id, Ingredient inputIngredient, NonNullList<IngredientChance> outputItems, FluidStack outputFluid, int duration) {
        super(id, inputIngredient, outputItems, outputFluid, duration);
    }

    @Override
    public NonNullList<IngredientChance> assemble(ItemStack inputItem) {
        return RecipeFacadeSqueeze.getOutput(inputItem);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_FACADE_SQUEEZE_MECHANICAL;
    }
}
