package org.cyclops.integrateddynamics.api.ingredient;

import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.init.IRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Registry for handling the translation between {@link IngredientComponent} instances and {@link IValue}.
 * @author rubensworks
 */
public interface IIngredientComponentHandlerRegistry extends IRegistry {

    /**
     * Register a new recipe component handler.
     * @param handler The new handler.
     * @param <VT> The value type.
     * @param <V> The value.
     * @param <T> The instance type.
     * @param <R> The recipe target type, may be Void.
     * @param <M> The matching condition parameter.
     * @param <C> The component type.
     * @param <H> The handler type.
     * @return The registered handler.
     */
    public <VT extends IValueType<V>, V extends IValue, T, R, M, C extends IngredientComponent<T, R, M>,
            H extends IIngredientComponentHandler<VT, V, T, R, M, C>> H register(H handler);

    /**
     * Get a handler by component type.
     * @param component The component type.
     * @param <VT> The value type.
     * @param <V> The value.
     * @param <T> The instance type.
     * @param <R> The recipe target type, may be Void.
     * @param <M> The matching condition parameter.
     * @param <C> The component type.
     * @return The handler or null.
     */
    @Nullable
    public <VT extends IValueType<V>, V extends IValue, T, R, M, C extends IngredientComponent<T, R, M>>
    IIngredientComponentHandler<VT, V, T, R, M, C> getComponentHandler(C component);

    /**
     * @return The recipe components that have a handler.
     */
    public Set<IngredientComponent<?, ?, ?>> getComponents();

}
