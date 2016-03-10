package org.cyclops.integrateddynamics.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
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
        IVariableFacade facade;
        int count = 0;
        for(int j = 0; j < inv.getSizeInventory(); j++) {
            ItemStack element = inv.getStackInSlot(j);
            if(element != null && element.getItem() instanceof ItemVariable) {
                count++;
                facade = ItemVariable.getInstance().getVariableFacade(element);
                if(!facade.isValid() && withoutData == null && element.stackSize == 1) {
                    withoutData = element;
                }
                if(facade.isValid() && withData == null && element.stackSize == 1) {
                    withData = element;
                }
            }
        }
        if(count == 2 && withoutData != null && withData != null) {
            return withData;
        }
        return null;
    }

    @Override
    public int getRecipeSize() {
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(ItemVariable.getInstance(), 1);
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv) {
        ItemStack[] ret = new ItemStack[inv.getSizeInventory()];
        for(int j = 0; j < inv.getSizeInventory(); j++) {
            ItemStack element = inv.getStackInSlot(j);
            if(element != null && element.getItem() instanceof ItemVariable) {
                IVariableFacade facade = ItemVariable.getInstance().getVariableFacade(element);
                if(facade.isValid()) {
                    // Create a copy with a new id.
                    ret[j] = IntegratedDynamics._instance.getRegistryManager()
                            .getRegistry(IVariableFacadeHandlerRegistry.class).copy(!MinecraftHelpers.isClientSide(), element);
                }
            }
        }
        return ret;
    }
}
