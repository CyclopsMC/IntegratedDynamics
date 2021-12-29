package org.cyclops.integrateddynamics.recipe;

import lombok.Getter;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;

import javax.annotation.Nullable;

/**
 * Recipe for combining facades with blocks.
 * @author rubensworks
 *
 */
public class ItemFacadeRecipe extends CustomRecipe {

    @Getter(lazy = true)
    private final NonNullList<Ingredient> ingredients =
            NonNullList.of(Ingredient.EMPTY, Ingredient.of(getResultItem()), new BlocksIngredient());

    public ItemFacadeRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingContainer grid, Level world) {
        return !assemble(grid).isEmpty();
    }

    @Override
    public ItemStack getResultItem() {
        return new ItemStack(RegistryEntries.ITEM_FACADE);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inventory) {
        NonNullList<ItemStack> aitemstack = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < aitemstack.size(); ++i) {
            ItemStack itemstack = inventory.getItem(i);
            aitemstack.set(i, net.minecraftforge.common.ForgeHooks.getContainerItem(itemstack));
        }

        return aitemstack;
    }

    @Override
    public ItemStack assemble(CraftingContainer grid) {
        ItemStack output = getResultItem().copy();

        int facades = 0;
        ItemStack block = ItemStack.EMPTY;

        for(int j = 0; j < grid.getContainerSize(); j++) {
            ItemStack element = grid.getItem(j);
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
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_FACADE;
    }

    public static class BlocksIngredient extends Ingredient {

        protected BlocksIngredient() {
            super(ForgeRegistries.BLOCKS.getValues().stream().map(ItemStack::new).map(Ingredient.ItemValue::new));
        }

        @Override
        public boolean test(@Nullable ItemStack itemStack) {
            return itemStack != null && !itemStack.isEmpty() && itemStack.getItem() instanceof BlockItem;
        }
    }

}
