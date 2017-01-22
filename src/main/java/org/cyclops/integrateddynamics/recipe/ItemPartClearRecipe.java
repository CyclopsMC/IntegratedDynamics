package org.cyclops.integrateddynamics.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
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
        return !getCraftingResult(inv).isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        for(int j = 0; j < inv.getSizeInventory(); j++) {
            ItemStack element = inv.getStackInSlot(j);
            if(!element.isEmpty() && element.getItem() instanceof ItemPart) {
                // Create copy of the stack WITHOUT the NBT tag.
                return new ItemStack(element.getItem(), 1, element.getItemDamage());
            }
        }
        return ItemStack.EMPTY;
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
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }
}
