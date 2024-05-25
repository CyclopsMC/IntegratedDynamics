package org.cyclops.integrateddynamics.core.recipe.type;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.recipe.ItemStackFromIngredient;
import org.cyclops.integrateddynamics.RegistryEntries;

import java.util.Objects;
import java.util.Optional;

/**
 * Squeezer recipe
 * @author rubensworks
 */
public class RecipeSqueezer implements Recipe<Container> {

    private final Ingredient inputIngredient;
    private final NonNullList<IngredientChance> outputItems;
    private final Optional<FluidStack> outputFluid;

    public RecipeSqueezer(Ingredient inputIngredient,
                          NonNullList<IngredientChance> outputItems,
                          Optional<FluidStack> outputFluid) {
        this.inputIngredient = inputIngredient;
        this.outputItems = outputItems;
        this.outputFluid = outputFluid;
    }

    public Ingredient getInputIngredient() {
        return inputIngredient;
    }

    public NonNullList<IngredientChance> getOutputItems() {
        return outputItems;
    }

    public Optional<FluidStack> getOutputFluid() {
        return outputFluid;
    }

    @Override
    public boolean matches(Container inv, Level worldIn) {
        return inputIngredient.test(inv.getItem(0));
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess registryAccess) {
        // Should not be called, but let's provide a good fallback
        if (this.outputItems.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return this.outputItems.get(0).getIngredientFirst().copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height <= 1;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        // Should not be called, but lets provide a good fallback
        if (this.outputItems.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return this.outputItems.get(0).getIngredientFirst().copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_SQUEEZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RegistryEntries.RECIPETYPE_SQUEEZER.get();
    }

    public static class IngredientChance {
        private final Either<Pair<ItemStack, Float>, Pair<ItemStackFromIngredient, Float>> ingredient;

        public IngredientChance(Either<Pair<ItemStack, Float>, Pair<ItemStackFromIngredient, Float>> ingredient) {
            this.ingredient = Objects.requireNonNull(ingredient);
        }

        public Either<ItemStack, ItemStackFromIngredient> getIngredient() {
            return ingredient.mapBoth(Pair::getLeft, Pair::getLeft);
        }

        public Either<Pair<ItemStack, Float>, Pair<ItemStackFromIngredient, Float>> getIngredientChance() {
            return ingredient;
        }

        public ItemStack getIngredientFirst() {
            return getIngredient().map(l -> l, ItemStackFromIngredient::getFirstItemStack);
        }

        public float getChance() {
            return ingredient.map(Pair::getRight, Pair::getRight);
        }

    }

}
