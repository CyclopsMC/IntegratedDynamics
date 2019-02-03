package org.cyclops.integrateddynamics.recipe;

import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.item.ItemFacade;

import java.util.stream.StreamSupport;

/**
 * Recipe for combining facades with blocks.
 * @author rubensworks
 *
 */
public class ItemFacadeRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	@Getter(lazy = true)
	private final NonNullList<Ingredient> ingredients =
			NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(getRecipeOutput()), new BlocksIngredient());

	@Override
	public boolean matches(InventoryCrafting grid, World world) {
		return !getCraftingResult(grid).isEmpty();
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
		ItemStack block = ItemStack.EMPTY;

		for(int j = 0; j < grid.getSizeInventory(); j++) {
			ItemStack element = grid.getStackInSlot(j);
			if(!element.isEmpty()) {
				if(element.getItem() == output.getItem()) {
					facades++;
				} else if(block.isEmpty() && element.getItem() instanceof ItemBlock) {
					block = element;
				} else {
					return ItemStack.EMPTY;
				}
			}
		}
		
		if(facades != 1 || block.isEmpty()) {
			return ItemStack.EMPTY;
		}
		
		ItemFacade.getInstance().writeFacadeBlock(output, BlockHelpers.getBlockStateFromItemStack(block));
		return output;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	public static class BlocksIngredient extends Ingredient {

		@Override
		public ItemStack[] getMatchingStacks() {
			return StreamSupport.stream(Block.REGISTRY.spliterator(), false)
					.map(ItemStack::new)
					.toArray(ItemStack[]::new);
		}

		@Override
		public boolean apply(ItemStack itemStack) {
			return !itemStack.isEmpty() && itemStack.getItem() instanceof ItemBlock;
		}
	}

}
