package org.cyclops.integrateddynamics.core.recipe.type;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Crafting recipe to clear item NBT data.
 * @author rubensworks
 */
public class RecipeNbtClear extends SpecialRecipe {

    private final Ingredient inputIngredient;

    public RecipeNbtClear(ResourceLocation id, Ingredient inputIngredient) {
        super(id);
        this.inputIngredient = inputIngredient;
    }

    public Ingredient getInputIngredient() {
        return inputIngredient;
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        return !getCraftingResult(inv).isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack ret = ItemStack.EMPTY;
        for(int j = 0; j < inv.getSizeInventory(); j++) {
            ItemStack element = inv.getStackInSlot(j);
            if(!element.isEmpty()) {
                if (this.inputIngredient.test(element)) {
                    if (!ret.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                    // Create copy of the stack WITHOUT the NBT tag.
                    ret = new ItemStack(element.getItem());
                } else {
                    return ItemStack.EMPTY;
                }
            }
        }
        return ret;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return inputIngredient.getMatchingStacks()[0]; // This is just a dummy item!
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(getRecipeOutput()));
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_NBT_CLEAR;
    }
}
