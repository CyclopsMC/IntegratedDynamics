package org.cyclops.integrateddynamics.recipe;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.item.ItemVariable;

/**
 * Crafting recipe to copy variable data.
 * @author rubensworks
 */
public class ItemVariableCopyRecipe extends SpecialRecipe {

    public ItemVariableCopyRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        return !assemble(inv).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInventory inv) {
        ItemStack withData = ItemStack.EMPTY;
        ItemStack withoutData = ItemStack.EMPTY;
        IVariableFacade facade;
        int count = 0;
        for(int j = 0; j < inv.getContainerSize(); j++) {
            ItemStack element = inv.getItem(j);
            if(!element.isEmpty() && element.getItem() instanceof ItemVariable) {
                count++;
                facade = RegistryEntries.ITEM_VARIABLE.getVariableFacade(element);
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
    public ItemStack getResultItem() {
        return new ItemStack(RegistryEntries.ITEM_VARIABLE, 1);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        NonNullList<ItemStack> ret = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
        for(int j = 0; j < inv.getContainerSize(); j++) {
            ItemStack element = inv.getItem(j);
            if(!element.isEmpty() && element.getItem() instanceof ItemVariable) {
                IVariableFacade facade = RegistryEntries.ITEM_VARIABLE.getVariableFacade(element);
                if(facade.isValid()) {
                    // Create a copy with a new id.
                    ret.set(j, IntegratedDynamics._instance.getRegistryManager()
                            .getRegistry(IVariableFacadeHandlerRegistry.class).copy(!MinecraftHelpers.isClientSideThread(), element));
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
    public IRecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_VARIABLE_COPY;
    }
}
