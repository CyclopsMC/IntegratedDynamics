package org.cyclops.integrateddynamics.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Crafting recipe to clear item NBT data.
 * @author rubensworks
 */
public class ItemNbtClearRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

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
        return !getCraftingResult(inv).isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack ret = ItemStack.EMPTY;
        for(int j = 0; j < inv.getSizeInventory(); j++) {
            ItemStack element = inv.getStackInSlot(j);
            if(!element.isEmpty()) {
                if (this.clazz.isInstance(element.getItem())) {
                    if (!ret.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                    // Create copy of the stack WITHOUT the NBT tag.
                    ret = new ItemStack(element.getItem(), 1, element.getItemDamage());
                } else {
                    return ItemStack.EMPTY;
                }
            }
        }
        return ret;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(dummyInstance, 1); // This is just a dummy item!
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(getRecipeOutput()));
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
}
