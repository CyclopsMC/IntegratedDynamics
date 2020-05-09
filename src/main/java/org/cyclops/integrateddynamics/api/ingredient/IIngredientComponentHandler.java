package org.cyclops.integrateddynamics.api.ingredient;

import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

import javax.annotation.Nullable;

/**
 * Handles the translation between {@link IngredientComponent} instances and {@link IValue}.
 * @param <VT> The value type.
 * @param <V> The value.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public interface IIngredientComponentHandler<VT extends IValueType<V>, V extends IValue, T, M> {

    /**
     * @return The value type with which the component should be handled.
     */
    public VT getValueType();

    /**
     * @return The recipe component.
     */
    public IngredientComponent<T, M> getComponent();

    /**
     * Convert an instance to a value.
     * @param instance A recipe component instance.
     * @return A value.
     */
    public V toValue(@Nullable T instance);

    /**
     * Convert a value to an instance.
     * @param value A value.
     * @return A recipe component instance.
     */
    @Nullable
    public T toInstance(V value);

    /**
     * Convert the given list of values to a compact string.
     * To be used in things like tooltips.
     * @param ingredientValue A list of ingredient values.
     * @return A compact string representation.
     */
    default public String toCompactString(V ingredientValue) {
        return getValueType().toCompactString(ingredientValue);
    }

}
