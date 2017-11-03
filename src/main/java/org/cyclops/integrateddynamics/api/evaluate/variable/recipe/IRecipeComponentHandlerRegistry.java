package org.cyclops.integrateddynamics.api.evaluate.variable.recipe;

import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeComponent;
import org.cyclops.cyclopscore.init.IRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Registry for handling the translation between {@link RecipeComponent} instances and {@link IValue}.
 * @author rubensworks
 */
public interface IRecipeComponentHandlerRegistry extends IRegistry {

    /**
     * Register a new recipe component handler.
     * @param <VT> The value type.
     * @param <V> The value.
     * @param <V> The vaue type
     * @param <T> The instance type.
     * @param <R> The recipe target type, may be Void.
     * @param <C> The component type.
     * @return The registered handler.
     */
    public <VT extends IValueType<V>, V extends IValue, T, R, C extends RecipeComponent<T, R>,
            H extends IRecipeComponentHandler<VT, V, T, R, C>> H register(H handler);

    /**
     * Get a handler by component type.
     * @param component The component type.
     * @param <T> The instance type.
     * @param <R> The recipe target type, may be Void.
     * @param <C> The component type.
     * @return The handler or null.
     */
    @Nullable
    public <VT extends IValueType<V>, V extends IValue, T, R, C extends RecipeComponent<T, R>>
    IRecipeComponentHandler<VT, V, T, R, C> getComponentHandler(C component);

    /**
     * @return The recipe components that have a handler.
     */
    public Set<RecipeComponent<?, ?>> getComponents();

}
