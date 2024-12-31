package org.cyclops.integrateddynamics.core.recipe.type;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.recipe.ItemStackFromIngredient;
import org.cyclops.integrateddynamics.RegistryEntries;

import java.util.Objects;

/**
 * Squeezer recipe
 * @author rubensworks
 */
public class RecipeSqueezer implements Recipe<Container> {

    private final ResourceLocation id;
    private final Ingredient inputIngredient;
    private final NonNullList<IngredientChance> outputItems;
    private final FluidStack outputFluid;

    public RecipeSqueezer(ResourceLocation id, Ingredient inputIngredient,
                          NonNullList<IngredientChance> outputItems, FluidStack outputFluid) {
        this.id = id;
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

    public FluidStack getOutputFluid() {
        return outputFluid;
    }

    @Override
    public boolean matches(Container inv, Level worldIn) {
        return inputIngredient.test(inv.getItem(0));
    }

    @Override
    public ItemStack assemble(Container inv) {
        // Should not be called, but let's provide a good fallback
        if (this.outputItems.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return this.outputItems.get(0).getIngredientFirst().copy();
    }

    public NonNullList<IngredientChance> assemble(ItemStack inputItem) {
        if (!inputItem.is(RegistryEntries.ITEM_FACADE.asItem())) return getOutputItems();
        return FacadeSqueezeCalculator.getOutputItems(inputItem);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height <= 1;
    }

    @Override
    public ItemStack getResultItem() {
        // Should not be called, but lets provide a good fallback
        if (this.outputItems.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return this.outputItems.get(0).getIngredientFirst().copy();
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_SQUEEZER;
    }

    @Override
    public RecipeType<?> getType() {
        return RegistryEntries.RECIPETYPE_SQUEEZER;
    }

    public static class IngredientChance {
        private final Either<ItemStack, ItemStackFromIngredient> ingredient;
        private final float chance;

        public IngredientChance(Either<ItemStack, ItemStackFromIngredient> ingredient, float chance) {
            this.ingredient = Objects.requireNonNull(ingredient);
            this.chance = chance;
        }

        public Either<ItemStack, ItemStackFromIngredient> getIngredient() {
            return ingredient;
        }

        public ItemStack getIngredientFirst() {
            return getIngredient().map(l -> l, ItemStackFromIngredient::getFirstItemStack);
        }

        public float getChance() {
            return chance;
        }

    }

}
