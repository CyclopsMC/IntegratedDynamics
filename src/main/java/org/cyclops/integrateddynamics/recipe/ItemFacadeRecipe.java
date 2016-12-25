package org.cyclops.integrateddynamics.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.item.ItemFacade;

/**
 * Recipe for combining facades with blocks.
 * @author rubensworks
 *
 */
public class ItemFacadeRecipe implements IRecipe {

	@Override
	public boolean matches(InventoryCrafting grid, World world) {
		return getCraftingResult(grid) != null;
	}
	
	@Override
	public int getRecipeSize() {
		return 2;
	}
	
	@Override
	public ItemStack getRecipeOutput() {
		return new ItemStack(ItemFacade.getInstance());
	}

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inventory) {
		NonNullList<ItemStack> aitemstack = NonNullList.withSize(inventory.getSizeInventory(), ItemStack.EMPTY);

        for (int i = 0; i < aitemstack.size(); ++i) {
            ItemStack itemstack = inventory.getStackInSlot(i);
            aitemstack.set(i, net.minecraftforge.common.ForgeHooks.getContainerItem(itemstack));
        }

        return aitemstack;
    }

    @Override
	public ItemStack getCraftingResult(InventoryCrafting grid) {						
		ItemStack output = getRecipeOutput().copy();

		int facades = 0;
		ItemStack block = null;

		for(int j = 0; j < grid.getSizeInventory(); j++) {
			ItemStack element = grid.getStackInSlot(j);
			if(element != null) {
				if(element.getItem() == output.getItem()) {
					facades++;
				} else if(block == null && element.getItem() instanceof ItemBlock) {
					block = element;
				} else {
					return null;
				}
			}
		}
		
		if(facades != 1 || block == null) {
			return null;
		}
		
		ItemFacade.getInstance().writeFacadeBlock(output, BlockHelpers.getBlockStateFromItemStack(block));
		return output;
	}
	
}
