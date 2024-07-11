package org.cyclops.integrateddynamics.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;
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
        for(int j = 0; j < inv.size(); j++) {
            ItemStack element = inv.getItem(j);
            if(!element.isEmpty() && element.getItem() instanceof ItemVariable) {
                IVariableFacade facade = RegistryEntries.ITEM_VARIABLE.get().getVariableFacade(lastValueDeseralizationContext, element);
                if(facade.isValid()) {
                    // Create a copy with a new id.
                    ItemStack copy = IntegratedDynamics._instance.getRegistryManager()
                            .getRegistry(IVariableFacadeHandlerRegistry.class).copy(!MinecraftHelpers.isClientSideThread(), element);

                    // If the input had a label, also copy the label
                    String label = LabelsWorldStorage.getInstance(IntegratedDynamics._instance).getLabel(facade.getId());
                    if(label != null) {
                        IVariableFacade facadeCopy = RegistryEntries.ITEM_VARIABLE.get().getVariableFacade(lastValueDeseralizationContext, copy);
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
        return RegistryEntries.RECIPESERIALIZER_VARIABLE_COPY.get();
    }
}
