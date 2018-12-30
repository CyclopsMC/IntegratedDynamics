package org.cyclops.integrateddynamics.core.ingredient;

import com.google.common.collect.Sets;
import org.cyclops.commoncapabilities.api.ingredient.IMixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;

import java.util.List;
import java.util.Set;

/**
 * A lazy extension for ingredients for a list of instances.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter, may be Void. Instances MUST properly implement the equals method.
 * @author rubensworks
 */
public class ExtendedIngredientsList<T, M> extends WrappedIngredients {

    private final IngredientComponent<T, M> component;
    private final List<T> instances;

    public ExtendedIngredientsList(IMixedIngredients base, IngredientComponent<T, M> component, List<T> instances) {
        super(base);
        this.component = component;
        this.instances = instances;
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
        return forComponent(ingredientComponent) ? (List<T2>) this.instances : superList;
    }
}
