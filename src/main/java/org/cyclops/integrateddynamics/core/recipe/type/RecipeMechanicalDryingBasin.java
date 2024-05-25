package org.cyclops.integrateddynamics.core.recipe.type;

import com.mojang.datafixers.util.Either;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.cyclopscore.recipe.ItemStackFromIngredient;
import org.cyclops.integrateddynamics.RegistryEntries;

import java.util.Optional;

/**
 * Mechanical drying basin recipe
 * @author rubensworks
 */
public class RecipeMechanicalDryingBasin extends RecipeDryingBasin {

    public RecipeMechanicalDryingBasin(Optional<Ingredient> inputIngredient, Optional<FluidStack> inputFluid,
                                       Optional<Either<ItemStack, ItemStackFromIngredient>> outputItem, Optional<FluidStack> outputFluid, int duration) {
        super(inputIngredient, inputFluid, outputItem, outputFluid, duration);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_MECHANICAL_DRYING_BASIN.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RegistryEntries.RECIPETYPE_MECHANICAL_DRYING_BASIN.get();
    }
}
