package org.cyclops.integrateddynamics.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.item.ItemVariable;

/**
 * Crafting recipe to copy variable data.
 * @author rubensworks
 */
public class ItemVariableCopyRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        return !getCraftingResult(inv).isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack withData = ItemStack.EMPTY;
        ItemStack withoutData = ItemStack.EMPTY;
        IVariableFacade facade;
        int count = 0;
        for(int j = 0; j < inv.getSizeInventory(); j++) {
            ItemStack element = inv.getStackInSlot(j);
            if(!element.isEmpty() && element.getItem() instanceof ItemVariable) {
                count++;
                facade = ItemVariable.getInstance().getVariableFacade(element);
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
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(ItemVariable.getInstance(), 1);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        NonNullList<ItemStack> ret = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        for(int j = 0; j < inv.getSizeInventory(); j++) {
            ItemStack element = inv.getStackInSlot(j);
            if(!element.isEmpty() && element.getItem() instanceof ItemVariable) {
                IVariableFacade facade = ItemVariable.getInstance().getVariableFacade(element);
                if(facade.isValid()) {
                    // Create a copy with a new id.
                    ret.set(j, IntegratedDynamics._instance.getRegistryManager()
                            .getRegistry(IVariableFacadeHandlerRegistry.class).copy(!MinecraftHelpers.isClientSide(), element));
                }
            }
        }
        return ret;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(getRecipeOutput()), Ingredient.fromStacks(getRecipeOutput()));
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
}
