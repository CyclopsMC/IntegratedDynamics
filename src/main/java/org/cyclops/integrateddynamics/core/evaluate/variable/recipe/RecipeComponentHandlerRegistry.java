package org.cyclops.integrateddynamics.core.evaluate.variable.recipe;

import com.google.common.collect.Maps;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeComponent;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.recipe.IRecipeComponentHandler;
import org.cyclops.integrateddynamics.api.evaluate.variable.recipe.IRecipeComponentHandlerRegistry;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author rubensworks
 */
public class RecipeComponentHandlerRegistry implements IRecipeComponentHandlerRegistry {

    private static RecipeComponentHandlerRegistry INSTANCE = new RecipeComponentHandlerRegistry();

    private final Map<RecipeComponent<?, ?>, IRecipeComponentHandler> componentTypes = Maps.newIdentityHashMap();

    private RecipeComponentHandlerRegistry() {

    }

    /**
     * @return The unique instance.
     */
    public static RecipeComponentHandlerRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public <VT extends IValueType<V>, V extends IValue,
            T, R, C extends RecipeComponent<T, R>, H extends IRecipeComponentHandler<VT, V, T, R, C>> H register(H handler) {
        this.componentTypes.put(Objects.requireNonNull(handler.getComponent(), "The recipe component of "
                + handler + " was null, it is probably not initialized yet!"), handler);
        return handler;
    }

    @Nullable
    @Override
    public <VT extends IValueType<V>, V extends IValue, T, R, C extends RecipeComponent<T, R>>
    IRecipeComponentHandler<VT, V, T, R, C> getComponentHandler(C component) {
        return this.componentTypes.get(component);
    }

    @Override
    public Set<RecipeComponent<?, ?>> getComponents() {
        return this.componentTypes.keySet();
    }
}
