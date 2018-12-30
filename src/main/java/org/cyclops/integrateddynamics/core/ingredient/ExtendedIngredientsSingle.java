package org.cyclops.integrateddynamics.core.ingredient;

import com.google.common.collect.Sets;
import org.cyclops.commoncapabilities.api.ingredient.IMixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;

import java.util.AbstractList;
import java.util.List;
import java.util.Set;

/**
 * A lazy extension for ingredients for a single instance.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter, may be Void. Instances MUST properly implement the equals method.
 * @author rubensworks
 */
public class ExtendedIngredientsSingle<T, M> extends WrappedIngredients {

    private final int targetIndex;

    private final IngredientComponent<T, M> component;
    private final T instance;

    public ExtendedIngredientsSingle(IMixedIngredients base, int targetIndex,
                                     IngredientComponent<T, M> component, T instance) {
        super(base);
        this.targetIndex = targetIndex;
        this.component = component;
        this.instance = instance;
    }

    protected boolean forComponent(IngredientComponent<?, ?> component) {
        return component == this.component;
    }

    @Override
    public Set<IngredientComponent<?, ?>> getComponents() {
        Set<IngredientComponent<?, ?>> components = Sets.newIdentityHashSet();
        components.addAll(super.getComponents());
        components.add(component);
        return components;
    }

    @Override
    public <T2> List<T2> getInstances(IngredientComponent<T2, ?> ingredientComponent) {
        List<T2> superList = super.getInstances(ingredientComponent);
        return forComponent(ingredientComponent) ? new AbstractList<T2>() {
            @Override
            public T2 get(int index) {
                if (index == targetIndex) {
                    return (T2) instance;
                } else if (index < targetIndex && index >= superList.size()) {
                    return (T2) component.getMatcher().getEmptyInstance();
                }
                return superList.get(index);
            }

            @Override
            public int size() {
                int superSize = superList.size();
                if (targetIndex >= superSize) {
                    return targetIndex + 1;
                }
                return superSize;
            }
        } : superList;
    }
}
