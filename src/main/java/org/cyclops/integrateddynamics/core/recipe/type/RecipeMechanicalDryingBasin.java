package org.cyclops.integrateddynamics.core.recipe.type;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Mechanical drying basin recipe
 * @author rubensworks
 */
public class RecipeMechanicalDryingBasin extends RecipeDryingBasin {

    public RecipeMechanicalDryingBasin(ResourceLocation id, Ingredient inputIngredient, FluidStack inputFluid,
                                       ItemStack outputItem, FluidStack outputFluid, int duration) {
        super(id, inputIngredient, inputFluid, outputItem, outputFluid, duration);
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_MECHANICAL_DRYING_BASIN;
    }

    @Override
    public IRecipeType<?> getType() {
        return RegistryEntries.RECIPETYPE_MECHANICAL_DRYING_BASIN;
    }
}
