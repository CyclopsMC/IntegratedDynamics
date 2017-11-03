package org.cyclops.integrateddynamics.api.evaluate.variable.recipe;

import com.google.common.collect.Iterables;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeComponent;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Handles the translation between {@link RecipeComponent} instances and {@link IValue}.
 * @param <VT> The value type.
 * @param <V> The value.
 * @param <T> The instance type.
 * @param <R> The recipe target type, may be Void.
 * @param <C> The component type.
 * @author rubensworks
 */
public interface IRecipeComponentHandler<VT extends IValueType<V>, V extends IValue,
        T, R, C extends RecipeComponent<T, R>> {

    /**
     * @return The value type with which the component should be handled.
     */
    public VT getValueType();

    /**
     * @return The recipe component.
     */
    public C getComponent();

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
    default public String toCompactString(List<V> ingredientValue) {
        String value = getValueType().toCompactString(Iterables.getFirst(ingredientValue,
                getValueType().getDefault()));
        if (ingredientValue.size() > 1) value += "+";
        return value;
    }

}
