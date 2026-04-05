package org.cyclops.integrateddynamics.core.recipe.type;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Crafting recipe to clear item NBT data.
 * @author rubensworks
 */
public class RecipeNbtClear extends CustomRecipe {

    private final Ingredient inputIngredient;

    public RecipeNbtClear(Ingredient inputIngredient) {
        super();
        this.inputIngredient = inputIngredient;
    }

    public Ingredient getInputIngredient() {
        return inputIngredient;
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {
        return !assemble(inv).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput inv) {
        ItemStack ret = ItemStack.EMPTY;
        for(int j = 0; j < inv.size(); j++) {
            ItemStack element = inv.getItem(j);
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

    public ItemStack getResultItem() {
        return new ItemStack(inputIngredient.items().findFirst().get()); // This is just a dummy item!
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        return NonNullList.withSize(inv.size(), ItemStack.EMPTY);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_NBT_CLEAR.get();
    }
}
