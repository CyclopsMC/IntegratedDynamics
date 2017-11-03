package org.cyclops.integrateddynamics.core.evaluate.variable.recipe;

import com.google.common.base.Predicates;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeComponent;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A wrapper around ingredients.
 * @author rubensworks
 */
public class WrappedIngredients implements IIngredients {

    private final IIngredients ingredients;

    public WrappedIngredients(IIngredients ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public Set<RecipeComponent<?, ?>> getComponents() {
        return ingredients.getComponents();
    }

    @Override
    public int getIngredients(RecipeComponent<?, ?> component) {
        return ingredients.getIngredients(component);
    }

    @Override
    public <V extends IValue, T, R> List<V> getList(RecipeComponent<T, R> component, int index) {
        List<V> list = ingredients.getList(component, index);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    @Override
    public <V extends IValue, T, R> Predicate<V> getPredicate(RecipeComponent<T, R> component, int index) {
        Predicate<V> predicate = ingredients.getPredicate(component, index);
        if (predicate == null) {
            return Predicates.alwaysFalse();
        }
        return predicate;
    }

    @Override
    public <V extends IValue, T, R> List<List<V>> getRaw(RecipeComponent<T, R> component) {
        List<List<V>> list = ingredients.getRaw(component);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }
}
