package org.cyclops.integrateddynamics.recipe;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;

import javax.annotation.Nullable;

/**
 * Recipe for combining facades with blocks.
 * @author rubensworks
 *
 */
public class ItemFacadeRecipe extends SpecialRecipe {

	private NonNullList<Ingredient> ingredients;

	public ItemFacadeRecipe(ResourceLocation id) {
		super(id);
	}

	public NonNullList<Ingredient> getIngredients() {
		if (ingredients == null) {
			// Catch runtime errors if other mods call this method before items have been registered
			try {
				ingredients = NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(getRecipeOutput()), new BlocksIngredient());
			} catch (RuntimeException e) {
				return NonNullList.create();
			}
		}
		return ingredients;
	}

	@Override
	public boolean matches(CraftingInventory grid, World world) {
		return !getCraftingResult(grid).isEmpty();
	}
	
	@Override
	public ItemStack getRecipeOutput() {
		return new ItemStack(RegistryEntries.ITEM_FACADE);
	}

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inventory) {
		NonNullList<ItemStack> aitemstack = NonNullList.withSize(inventory.getSizeInventory(), ItemStack.EMPTY);

        for (int i = 0; i < aitemstack.size(); ++i) {
            ItemStack itemstack = inventory.getStackInSlot(i);
            aitemstack.set(i, net.minecraftforge.common.ForgeHooks.getContainerItem(itemstack));
        }

        return aitemstack;
    }

    @Override
	public ItemStack getCraftingResult(CraftingInventory grid) {
		ItemStack output = getRecipeOutput().copy();

		int facades = 0;
		ItemStack block = ItemStack.EMPTY;

		for(int j = 0; j < grid.getSizeInventory(); j++) {
			ItemStack element = grid.getStackInSlot(j);
			if(!element.isEmpty()) {
				if(element.getItem() == output.getItem()) {
					facades++;
				} else if(block.isEmpty() && element.getItem() instanceof BlockItem) {
					block = element;
				} else {
					return ItemStack.EMPTY;
				}
			}
		}
		
		if(facades != 1 || block.isEmpty()) {
			return ItemStack.EMPTY;
		}

		RegistryEntries.ITEM_FACADE.writeFacadeBlock(output, BlockHelpers.getBlockStateFromItemStack(block));
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

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return RegistryEntries.RECIPESERIALIZER_FACADE;
	}

	public static class BlocksIngredient extends Ingredient {

		protected BlocksIngredient() {
			super(ForgeRegistries.BLOCKS.getValues().stream().map(ItemStack::new).map(Ingredient.SingleItemList::new));
		}

		@Override
		public boolean test(@Nullable ItemStack itemStack) {
			return itemStack != null && !itemStack.isEmpty() && itemStack.getItem() instanceof BlockItem;
		}
	}

}
