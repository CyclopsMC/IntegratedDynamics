package org.cyclops.integrateddynamics.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;
import org.cyclops.integrateddynamics.item.ItemVariable;

/**
 * Crafting recipe to copy variable data.
 * @author rubensworks
 */
public class ItemVariableCopyRecipe extends CustomRecipe {

    public ItemVariableCopyRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        return !assemble(inv).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
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
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        NonNullList<ItemStack> ret = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
        for(int j = 0; j < inv.getContainerSize(); j++) {
            ItemStack element = inv.getItem(j);
            if(!element.isEmpty() && element.getItem() instanceof ItemVariable) {
                IVariableFacade facade = RegistryEntries.ITEM_VARIABLE.getVariableFacade(element);
                if(facade.isValid()) {
                    // Create a copy with a new id.
                    ItemStack copy = IntegratedDynamics._instance.getRegistryManager()
                            .getRegistry(IVariableFacadeHandlerRegistry.class).copy(!MinecraftHelpers.isClientSideThread(), element);

                    // If the input had a label, also copy the label
                    String label = LabelsWorldStorage.getInstance(IntegratedDynamics._instance).getLabel(facade.getId());
                    if(label != null) {
                        IVariableFacade facadeCopy = RegistryEntries.ITEM_VARIABLE.getVariableFacade(copy);
                        if (facadeCopy != null) {
                            LabelsWorldStorage.getInstance(IntegratedDynamics._instance).put(facadeCopy.getId(), label);
                        }
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
        return RegistryEntries.RECIPESERIALIZER_VARIABLE_COPY;
    }
}
