package org.cyclops.integrateddynamics.core.recipe.type;

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
import org.cyclops.integrateddynamics.RegistryEntries;

import java.util.Objects;

/**
 * Squeezer recipe
 * @author rubensworks
 */
public class RecipeSqueezer implements Recipe<Container> {

    private final ResourceLocation id;
    private final Ingredient inputIngredient;
    private final NonNullList<ItemStackChance> outputItems;
    private final FluidStack outputFluid;

    public RecipeSqueezer(ResourceLocation id, Ingredient inputIngredient,
                          NonNullList<ItemStackChance> outputItems, FluidStack outputFluid) {
        this.id = id;
        this.inputIngredient = inputIngredient;
        this.outputItems = outputItems;
        this.outputFluid = outputFluid;
    }

    public Ingredient getInputIngredient() {
        return inputIngredient;
    }

    public NonNullList<ItemStackChance> getOutputItems() {
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
        // Should not be called, but lets provide a good fallback
        if (this.outputItems.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return this.outputItems.get(0).getItemStack().copy();
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
        return this.outputItems.get(0).getItemStack().copy();
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

    public static class ItemStackChance {

        private final ItemStack itemStack;
        private final float chance;

        public ItemStackChance(ItemStack itemStack, float chance) {
            this.itemStack = Objects.requireNonNull(itemStack);
            this.chance = chance;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public float getChance() {
            return chance;
        }

    }

}
