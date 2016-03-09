package org.cyclops.integrateddynamics.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.item.ItemVariable;

/**
 * Crafting recipe to clear variable data.
 * @author rubensworks
 */
public class ItemVariableClearRecipe implements IRecipe {
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        return getCraftingResult(inv) != null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        int count = 0;
        for(int j = 0; j < inv.getSizeInventory(); j++) {
            ItemStack element = inv.getStackInSlot(j);
            if(element != null && element.getItem() instanceof ItemVariable) {
                count++;
            }
        }
        if(count == 1) {
            return getRecipeOutput();
        }
        return null;
    }

    @Override
    public int getRecipeSize() {
        return 1;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(ItemVariable.getInstance(), 1);
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv) {
        return new ItemStack[inv.getSizeInventory()];
    }
}
