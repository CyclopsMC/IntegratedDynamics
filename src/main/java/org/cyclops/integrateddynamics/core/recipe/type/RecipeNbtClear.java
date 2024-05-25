package org.cyclops.integrateddynamics.core.recipe.type;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
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

    private final Ingredient inputIngredient;

    public RecipeNbtClear(Ingredient inputIngredient) {
        super(CraftingBookCategory.MISC);
        this.inputIngredient = inputIngredient;
    }

    public Ingredient getInputIngredient() {
        return inputIngredient;
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        return !assemble(inv, worldIn.registryAccess()).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        ItemStack ret = ItemStack.EMPTY;
        for(int j = 0; j < inv.getContainerSize(); j++) {
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
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return getResultItem();
    }

    public ItemStack getResultItem() {
        return inputIngredient.getItems()[0]; // This is just a dummy item!
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        return NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
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
}
