package org.cyclops.integrateddynamics.capability.ingredient;

import net.minecraftforge.fluids.FluidStack;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integrateddynamics.api.ingredient.capability.IIngredientComponentValueHandler;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import javax.annotation.Nullable;

/**
 * @author rubensworks
 */
public class IngredientComponentValueHandlerFluidStack implements IIngredientComponentValueHandler<ValueObjectTypeFluidStack,
        ValueObjectTypeFluidStack.ValueFluidStack, FluidStack, Integer> {

    private final IngredientComponent<FluidStack, Integer> ingredientComponent;

    public IngredientComponentValueHandlerFluidStack(IngredientComponent<FluidStack, Integer> ingredientComponent) {
        this.ingredientComponent = ingredientComponent;
    }

    @Override
    public ValueObjectTypeFluidStack getValueType() {
        return ValueTypes.OBJECT_FLUIDSTACK;
    }

    @Override
    public IngredientComponent<FluidStack, Integer> getComponent() {
        return ingredientComponent;
    }

    @Override
    public ValueObjectTypeFluidStack.ValueFluidStack toValue(@Nullable FluidStack instance) {
        return ValueObjectTypeFluidStack.ValueFluidStack.of(instance);
    }

    @Override
    @Nullable
    public FluidStack toInstance(ValueObjectTypeFluidStack.ValueFluidStack value) {
        return value.getRawValue().orNull();
    }

}
