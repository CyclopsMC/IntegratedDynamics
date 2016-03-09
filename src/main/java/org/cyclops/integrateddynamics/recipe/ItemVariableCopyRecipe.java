package org.cyclops.integrateddynamics.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.item.ItemVariable;

/**
 * Crafting recipe to copy variable data.
 * @author rubensworks
 */
public class ItemVariableCopyRecipe implements IRecipe {
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        return getCraftingResult(inv) != null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack withData = null;
        ItemStack withoutData = null;
        int count = 0;
        for(int j = 0; j < inv.getSizeInventory(); j++) {
            ItemStack element = inv.getStackInSlot(j);
            if(element != null && element.getItem() instanceof ItemVariable) {
                count++;
                IVariableFacade facade = ItemVariable.getInstance().getVariableFacade(element);
                if(!facade.isValid() && withoutData == null) {
                    withoutData = element;
                }
                if(facade.isValid() && withData == null) {
                    withData = element;
                }
            }
        }
        if(count == 2 && withoutData != null && withData != null) {
            ItemStack result = withData.copy();
            result.stackSize = 2;
            return result;
        }
        return null;
    }

    @Override
    public int getRecipeSize() {
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(ItemVariable.getInstance(), 2);
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv) {
        return new ItemStack[inv.getSizeInventory()];
    }
}
