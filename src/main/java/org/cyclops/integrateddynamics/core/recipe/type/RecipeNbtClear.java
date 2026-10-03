package org.cyclops.integrateddynamics.core.recipe.type;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.cyclops.integrateddynamics.RegistryEntries;

import java.lang.ref.WeakReference;

/**
 * Crafting recipe to clear item NBT data.
 * @author rubensworks
 */
public class RecipeNbtClear extends CustomRecipe {

    private static final ThreadLocal<Leftover> PROTECTED_LEFTOVER = new ThreadLocal<>();

    private final Ingredient inputIngredient;

    public RecipeNbtClear(Ingredient inputIngredient) {
        super();
        this.inputIngredient = inputIngredient;
    }

    public Ingredient getInputIngredient() {
        return inputIngredient;
    }

    /**
     * Protect an item stack that another recipe leaves behind in the crafting grid,
     * so that this recipe does not clear it during the same crafting operation.
     *
     * Shift-clicking a crafting result keeps crafting for as long as the result stays the same item,
     * and this recipe matches any single item that it can clear.
     * Without this, an item that was just copied would be cleared again right after. (#725)
     *
     * @param itemStack The item stack that is left behind.
     *                  This must be the exact instance that ends up in the crafting grid.
     * @param level The level that is being crafted in.
     */
    public static void protectLeftover(ItemStack itemStack, Level level) {
        PROTECTED_LEFTOVER.set(new Leftover(new WeakReference<>(itemStack), level.getGameTime()));
    }

    protected boolean isProtected(CraftingInput inv, Level level) {
        Leftover leftover = PROTECTED_LEFTOVER.get();
        if (leftover == null || leftover.gameTime() != level.getGameTime()) {
            return false;
        }
        ItemStack protectedItemStack = leftover.itemStack().get();
        if (protectedItemStack == null) {
            return false;
        }
        for (int j = 0; j < inv.size(); j++) {
            if (inv.getItem(j) == protectedItemStack) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {
        return !isProtected(inv, worldIn) && !assemble(inv).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput inv) {
        ItemStack ret = ItemStack.EMPTY;
        for(int j = 0; j < inv.size(); j++) {
            ItemStack element = inv.getItem(j);
            if(!element.isEmpty()) {
                if (this.inputIngredient.test(element)) {
                    if (!ret.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                    // Create copy of the stack WITHOUT the NBT tag.
                    ret = new ItemStack(element.getItem());
                } else {
                    return ItemStack.EMPTY;
                }
            }
        }
        return ret;
    }

    public ItemStack getResultItem() {
        return new ItemStack(inputIngredient.items().findFirst().get()); // This is just a dummy item!
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        return NonNullList.withSize(inv.size(), ItemStack.EMPTY);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_NBT_CLEAR.get();
    }

    /**
     * An item stack that is left behind in a crafting grid, and the tick it was left behind in.
     * The item stack is held weakly, as one that is no longer reachable can not be in a crafting grid.
     */
    protected record Leftover(WeakReference<ItemStack> itemStack, long gameTime) {
    }
}
