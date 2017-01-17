package org.cyclops.integrateddynamics.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.core.item.ItemPart;
import org.cyclops.integrateddynamics.core.part.PartTypes;

/**
 * Crafting recipe to clear part data.
 * @author rubensworks
 */
public class ItemPartClearRecipe implements IRecipe {
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        return getCraftingResult(inv) != null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        for(int j = 0; j < inv.getSizeInventory(); j++) {
            ItemStack element = inv.getStackInSlot(j);
            if(element != null && element.getItem() instanceof ItemPart) {
                // Create copy of the stack WITHOUT the NBT tag.
                return new ItemStack(element.getItem(), 1, element.getItemDamage());
            }
        }
        return null;
    }

    @Override
    public int getRecipeSize() {
        return 1;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(PartTypes.REDSTONE_READER.getItem(), 1); // This is just a dummy item!
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv) {
        return new ItemStack[inv.getSizeInventory()];
    }
}
