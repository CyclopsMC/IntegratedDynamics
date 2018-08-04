package org.cyclops.integrateddynamics.capability.ingredient;

import net.minecraft.item.ItemStack;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integrateddynamics.api.ingredient.capability.IIngredientComponentValueHandler;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * @author rubensworks
 */
public class IngredientComponentValueHandlerItemStack implements IIngredientComponentValueHandler<ValueObjectTypeItemStack,
        ValueObjectTypeItemStack.ValueItemStack, ItemStack, Integer> {

    private final IngredientComponent<ItemStack, Integer> ingredientComponent;

    public IngredientComponentValueHandlerItemStack(IngredientComponent<ItemStack, Integer> ingredientComponent) {
        this.ingredientComponent = ingredientComponent;
    }

    @Override
    public ValueObjectTypeItemStack getValueType() {
        return ValueTypes.OBJECT_ITEMSTACK;
    }

    @Override
    public IngredientComponent<ItemStack, Integer> getComponent() {
        return ingredientComponent;
    }

    @Override
    public ValueObjectTypeItemStack.ValueItemStack toValue(ItemStack instance) {
        return ValueObjectTypeItemStack.ValueItemStack.of(instance);
    }

    @Override
    public ItemStack toInstance(ValueObjectTypeItemStack.ValueItemStack value) {
        return value.getRawValue();
    }

}
