package org.cyclops.integrateddynamics.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.CommonHooks;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Recipe for combining facades with blocks.
 * @author rubensworks
 *
 */
public class ItemFacadeRecipe extends CustomRecipe {

    private NonNullList<Ingredient> ingredients;

    public ItemFacadeRecipe(CraftingBookCategory craftingBookCategory) {
        super(craftingBookCategory);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        if (ingredients == null) {
            // Catch runtime errors if other mods call this method before items have been registered
            try {
                ingredients = NonNullList.of(Ingredient.EMPTY, Ingredient.of(getResultItem()), Ingredient.of(BuiltInRegistries.BLOCK.stream().map(ItemStack::new)));
            } catch (RuntimeException e) {
                return NonNullList.create();
            }
        }
        return ingredients;
    }

    @Override
    public boolean matches(CraftingInput grid, Level world) {
        return !assemble(grid, world.registryAccess()).isEmpty();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
        return getResultItem();
    }

    public ItemStack getResultItem() {
        return new ItemStack(RegistryEntries.ITEM_FACADE);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inventory) {
        NonNullList<ItemStack> aitemstack = NonNullList.withSize(inventory.size(), ItemStack.EMPTY);

        for (int i = 0; i < aitemstack.size(); ++i) {
            ItemStack itemstack = inventory.getItem(i);
            aitemstack.set(i, CommonHooks.getCraftingRemainingItem(itemstack));
        }

        return aitemstack;
    }

    @Override
    public ItemStack assemble(CraftingInput grid, HolderLookup.Provider registryAccess) {
        ItemStack output = getResultItem(registryAccess).copy();

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

        RegistryEntries.ITEM_FACADE.get().writeFacadeBlock(output, BlockHelpers.getBlockStateFromItemStack(block));
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
        return RegistryEntries.RECIPESERIALIZER_FACADE.get();
    }

}
