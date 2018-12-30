package org.cyclops.integrateddynamics.core.ingredient;

import com.google.common.collect.Maps;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentHandler;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentHandlerRegistry;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author rubensworks
 */
public class IngredientComponentHandlerRegistry implements IIngredientComponentHandlerRegistry {

    private static IngredientComponentHandlerRegistry INSTANCE = new IngredientComponentHandlerRegistry();

    private final Map<IngredientComponent<?, ?>, IIngredientComponentHandler> componentTypes = Maps.newIdentityHashMap();

    private IngredientComponentHandlerRegistry() {

    }

    /**
     * @return The unique instance.
     */
    public static IngredientComponentHandlerRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public <VT extends IValueType<V>, V extends IValue,
            T, M, H extends IIngredientComponentHandler<VT, V, T, M>> H register(H handler) {
        this.componentTypes.put(Objects.requireNonNull(handler.getComponent(), "The recipe component of "
                + handler + " was null, it is probably not initialized yet!"), handler);
        return handler;
    }

    @Nullable
    @Override
    public <VT extends IValueType<V>, V extends IValue, T, M>
    IIngredientComponentHandler<VT, V, T, M> getComponentHandler(IngredientComponent<T, M> component) {
        return this.componentTypes.get(component);
    }

    @Override
    public Set<IngredientComponent<?, ?>> getComponents() {
        return this.componentTypes.keySet();
    }
}
