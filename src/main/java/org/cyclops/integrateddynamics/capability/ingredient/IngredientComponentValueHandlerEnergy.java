package org.cyclops.integrateddynamics.capability.ingredient;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integrateddynamics.api.ingredient.capability.IIngredientComponentValueHandler;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import javax.annotation.Nullable;

/**
 * @author rubensworks
 */
public class IngredientComponentValueHandlerEnergy implements IIngredientComponentValueHandler<ValueTypeInteger,
        ValueTypeInteger.ValueInteger, Integer, Boolean> {

    private final IngredientComponent<Integer, Boolean> ingredientComponent;

    public IngredientComponentValueHandlerEnergy(IngredientComponent<Integer, Boolean> ingredientComponent) {
        this.ingredientComponent = ingredientComponent;
    }

    @Override
    public ValueTypeInteger getValueType() {
        return ValueTypes.INTEGER;
    }

    @Override
    public IngredientComponent<Integer, Boolean> getComponent() {
        return ingredientComponent;
    }

    @Override
    public ValueTypeInteger.ValueInteger toValue(@Nullable Integer instance) {
        return ValueTypeInteger.ValueInteger.of(instance);
    }

    @Nullable
    @Override
    public Integer toInstance(ValueTypeInteger.ValueInteger value) {
        return value.getRawValue();
    }

    @Override
    public Component toCompactString(ValueTypeInteger.ValueInteger ingredientValue) {
        return getValueType().toCompactString(ingredientValue)
                .append(" ")
                .append(new TranslatableComponent(L10NValues.GENERAL_ENERGY_UNIT));
    }

}
