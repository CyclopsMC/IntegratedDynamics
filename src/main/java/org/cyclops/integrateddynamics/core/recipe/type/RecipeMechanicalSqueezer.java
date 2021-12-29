package org.cyclops.integrateddynamics.core.recipe.type;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
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
    public RecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_MECHANICAL_SQUEEZER;
    }

    @Override
    public RecipeType<?> getType() {
        return RegistryEntries.RECIPETYPE_MECHANICAL_SQUEEZER;
    }
}
