package org.cyclops.integrateddynamics.core.recipe.type;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
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
    public RecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_MECHANICAL_DRYING_BASIN;
    }

    @Override
    public RecipeType<?> getType() {
        return RegistryEntries.RECIPETYPE_MECHANICAL_DRYING_BASIN;
    }
}
