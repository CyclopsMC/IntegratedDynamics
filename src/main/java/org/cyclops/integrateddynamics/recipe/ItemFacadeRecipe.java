package org.cyclops.integrateddynamics.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Recipe for combining facades with blocks.
 * @author rubensworks
 *
 */
public class ItemFacadeRecipe extends CustomRecipe {

    private NonNullList<Ingredient> ingredients;

    public ItemFacadeRecipe() {
        super();
    }

    @Override
    public boolean matches(CraftingInput grid, Level world) {
        return !assemble(grid).isEmpty();
    }

    public ItemStack getResultItem() {
        return new ItemStack(RegistryEntries.ITEM_FACADE);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inventory) {
        NonNullList<ItemStack> aitemstack = NonNullList.withSize(inventory.size(), ItemStack.EMPTY);

        for (int i = 0; i < aitemstack.size(); ++i) {
            ItemStack itemstack = inventory.getItem(i);
            net.minecraft.world.item.ItemStackTemplate remainder = itemstack.getItem().getCraftingRemainder(itemstack);
            aitemstack.set(i, remainder != null ? remainder.create() : ItemStack.EMPTY);
        }

        return aitemstack;
    }

    @Override
    public ItemStack assemble(CraftingInput grid) {
        ItemStack output = getResultItem().copy();

        int facades = 0;
        ItemStack block = ItemStack.EMPTY;

        for(int j = 0; j < grid.size(); j++) {
            ItemStack element = grid.getItem(j);
            if(!element.isEmpty()) {
                if(element.getItem() == output.getItem()) {
                    facades++;
                } else if(block.isEmpty() && element.getItem() instanceof BlockItem
                        && !((BlockItem) element.getItem()).getBlock().defaultBlockState().useShapeForLightOcclusion()) {
                    block = element;
                } else {
                    return ItemStack.EMPTY;
                }
            }
        }

        if(facades != 1 || block.isEmpty()) {
            return ItemStack.EMPTY;
        }

        RegistryEntries.ITEM_FACADE.get().writeFacadeBlock(output, IModHelpers.get().getBlockHelpers().getBlockStateFromItemStack(block));
        return output;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_FACADE.get();
    }

}
