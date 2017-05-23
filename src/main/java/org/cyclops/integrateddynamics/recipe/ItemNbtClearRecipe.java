package org.cyclops.integrateddynamics.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

/**
 * Crafting recipe to clear item NBT data.
 * @author rubensworks
 */
public class ItemNbtClearRecipe implements IRecipe {

    private final Class<? extends Item> clazz;
    private final Item dummyInstance;

    public ItemNbtClearRecipe(Class<? extends Item> clazz, Item dummyInstance) {
        this.clazz = clazz;
        this.dummyInstance = dummyInstance;
    }

    public ItemNbtClearRecipe(Item dummyInstance) {
        this(dummyInstance.getClass(), dummyInstance);
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        return getCraftingResult(inv) != null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack ret = null;
        for(int j = 0; j < inv.getSizeInventory(); j++) {
            ItemStack element = inv.getStackInSlot(j);
            if(element != null && this.clazz.isInstance(element.getItem())) {
                if (ret != null) {
                    return null;
                }
                // Create copy of the stack WITHOUT the NBT tag.
                ret = new ItemStack(element.getItem(), 1, element.getItemDamage());
            }
        }
        return ret;
    }

    @Override
    public int getRecipeSize() {
        return 1;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(dummyInstance, 1); // This is just a dummy item!
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv) {
        return new ItemStack[inv.getSizeInventory()];
    }
}
