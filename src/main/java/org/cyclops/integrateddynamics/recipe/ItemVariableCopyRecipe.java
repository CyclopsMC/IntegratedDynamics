package org.cyclops.integrateddynamics.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.CommonHooks;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.core.helper.PartConfigHelpers;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeNbtClear;
import org.cyclops.integrateddynamics.item.ItemVariable;

/**
 * Crafting recipe to copy variable data.
 * @author rubensworks
 */
public class ItemVariableCopyRecipe extends CustomRecipe {

    private ValueDeseralizationContext lastValueDeseralizationContext;

    public ItemVariableCopyRecipe(CraftingBookCategory craftingBookCategory) {
        super(craftingBookCategory);
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {
        lastValueDeseralizationContext = ValueDeseralizationContext.of(worldIn);
        return !assemble(inv, worldIn.registryAccess()).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registryAccess) {
        ItemStack withData = ItemStack.EMPTY;
        ItemStack withoutData = ItemStack.EMPTY;
        IVariableFacade facade;
        int count = 0;
        for(int j = 0; j < inv.size(); j++) {
            ItemStack element = inv.getItem(j);
            if(!element.isEmpty() && element.getItem() instanceof ItemVariable) {
                count++;
                facade = RegistryEntries.ITEM_VARIABLE.get().getVariableFacade(lastValueDeseralizationContext, element);
                if(!facade.isValid() && withoutData.isEmpty()) {
                    withoutData = element;
                }
                if(facade.isValid() && withData.isEmpty() && element.getCount() == 1) {
                    withData = element.copy();
                }
            }
        }
        if(count == 2 && !withoutData.isEmpty() && !withData.isEmpty()) {
            return withData;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
        return getResultItem();
    }

    public ItemStack getResultItem() {
        return new ItemStack(RegistryEntries.ITEM_VARIABLE, 1);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        NonNullList<ItemStack> ret = NonNullList.withSize(inv.size(), ItemStack.EMPTY);
        Player craftingPlayer = CommonHooks.getCraftingPlayer();
        for(int j = 0; j < inv.size(); j++) {
            ItemStack element = inv.getItem(j);
            if(!element.isEmpty() && element.getItem() instanceof ItemVariable) {
                IVariableFacade facade = RegistryEntries.ITEM_VARIABLE.get().getVariableFacade(lastValueDeseralizationContext, element);
                if(facade.isValid()) {
                    // Create a copy with a new id, and copy the label of the input as well.
                    ItemStack copy = PartConfigHelpers.copyVariable(lastValueDeseralizationContext, element,
                            !MinecraftHelpers.isClientSideThread());

                    // This copy stays behind in the crafting grid, where it is a single variable card,
                    // which is exactly what the recipe for clearing variable cards matches.
                    // Protect it, so that shift-clicking the result does not clear it right away. (Closes #725)
                    if (craftingPlayer != null) {
                        RecipeNbtClear.protectLeftover(copy, craftingPlayer.level());
                    }

                    ret.set(j, copy);
                }
            }
        }
        return ret;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, Ingredient.of(getResultItem()), Ingredient.of(getResultItem()));
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_VARIABLE_COPY.get();
    }
}
