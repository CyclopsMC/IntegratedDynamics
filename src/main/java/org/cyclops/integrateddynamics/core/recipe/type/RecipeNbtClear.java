package org.cyclops.integrateddynamics.core.recipe.type;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Crafting recipe to clear item NBT data.
 * @author rubensworks
 */
public class RecipeNbtClear extends CustomRecipe {

    private static final ThreadLocal<Leftover> PROTECTED_LEFTOVER = new ThreadLocal<>();

    private final Ingredient inputIngredient;

    public RecipeNbtClear(Ingredient inputIngredient) {
        super(CraftingBookCategory.MISC);
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
        PROTECTED_LEFTOVER.set(new Leftover(itemStack, level.getGameTime()));
    }

    protected boolean isProtected(CraftingInput inv, Level level) {
        Leftover leftover = PROTECTED_LEFTOVER.get();
        if (leftover == null || leftover.gameTime() != level.getGameTime()) {
            return false;
        }
        for (int j = 0; j < inv.size(); j++) {
            if (inv.getItem(j) == leftover.itemStack()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {
        return !isProtected(inv, worldIn) && !assemble(inv, worldIn.registryAccess()).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registryAccess) {
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

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
        return getResultItem();
    }

    public ItemStack getResultItem() {
        return inputIngredient.getItems()[0]; // This is just a dummy item!
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        return NonNullList.withSize(inv.size(), ItemStack.EMPTY);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, Ingredient.of(getResultItem()));
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_NBT_CLEAR.get();
    }

    /**
     * An item stack that is left behind in a crafting grid, and the tick it was left behind in.
     */
    protected record Leftover(ItemStack itemStack, long gameTime) {
    }
}
