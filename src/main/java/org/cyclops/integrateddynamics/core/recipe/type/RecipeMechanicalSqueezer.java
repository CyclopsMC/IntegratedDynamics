package org.cyclops.integrateddynamics.core.recipe.type;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.integrateddynamics.RegistryEntries;

import java.util.Optional;

/**
 * Mechanical squeezer recipe
 * @author rubensworks
 */
public class RecipeMechanicalSqueezer extends RecipeSqueezer {

    private final int duration;

    public RecipeMechanicalSqueezer(Ingredient inputIngredient,
                                    NonNullList<IngredientChance> outputItems,
                                    Optional<FluidStack> outputFluid,
                                    int duration) {
        super(inputIngredient, outputItems, outputFluid);
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_MECHANICAL_SQUEEZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RegistryEntries.RECIPETYPE_MECHANICAL_SQUEEZER.get();
    }
}
