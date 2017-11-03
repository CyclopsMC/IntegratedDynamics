package org.cyclops.integrateddynamics.core.evaluate.variable.recipe;

import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeComponent;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * An implementation of {@link IIngredients} by storing lists of lists for each ingredient type.
 *
 * The goal of this interface is only to provide a list-based basis for ingredients.
 * No custom predicates are supported as the implementation only checks for containment in lists.
 *
 * @author rubensworks
 */
public class IngredientsRecipeLists implements IIngredients {

    private final Map<RecipeComponent<?, ?>, List<List<? extends IValue>>> lists;

    public IngredientsRecipeLists(Map<RecipeComponent<?, ?>, List<List<? extends IValue>>> lists) {
        this.lists = lists;
    }

    @Override
    public boolean equals(Object obj) {
        return IIngredients.equals(this, obj);
    }

    @Override
    public String toString() {
        return lists.toString();
    }

    @Override
    public Set<RecipeComponent<?, ?>> getComponents() {
        return lists.keySet();
    }

    @Override
    public int getIngredients(RecipeComponent<?, ?> component) {
        return lists.getOrDefault(component, Collections.emptyList()).size();
    }

    @Override
    public <V extends IValue, T, R> List<V> getList(RecipeComponent<T, R> component, int index) {
        List<V> list = (List<V>) lists.get(component).get(index);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    @Override
    public <V extends IValue, T, R> Predicate<V> getPredicate(RecipeComponent<T, R> component, int index) {
        return lists.get(component)::contains;
    }

    @Override
    public <V extends IValue, T, R> List<List<V>> getRaw(RecipeComponent<T, R> component) {
        List<List<V>> list = (List) lists.get(component);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }
}
