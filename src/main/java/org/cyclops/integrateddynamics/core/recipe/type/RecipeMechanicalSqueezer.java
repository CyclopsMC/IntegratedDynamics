package org.cyclops.integrateddynamics.core.recipe.type;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Mechanical squeezer recipe
 * @author rubensworks
 */
public class RecipeMechanicalSqueezer extends RecipeSqueezer {

    private final int duration;

    public RecipeMechanicalSqueezer(ResourceLocation id, Ingredient inputIngredient,
                                    NonNullList<ItemStackChance> outputItems, FluidStack outputFluid, int duration) {
        super(id, inputIngredient, outputItems, outputFluid);
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_MECHANICAL_SQUEEZER;
    }

    @Override
    public IRecipeType<?> getType() {
        return RegistryEntries.RECIPETYPE_MECHANICAL_SQUEEZER;
    }
}
