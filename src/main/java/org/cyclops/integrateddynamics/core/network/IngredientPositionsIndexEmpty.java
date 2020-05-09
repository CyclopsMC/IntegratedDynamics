package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.Iterators;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientPositionsIndex;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PrioritizedPartPos;

import java.util.Iterator;

/**
 * An empty ingredients positions index.
 * @param <T> An instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public class IngredientPositionsIndexEmpty<T, M> implements IIngredientPositionsIndex<T, M> {

    private final IngredientComponent<T, M> component;

    public IngredientPositionsIndexEmpty(IngredientComponent<T, M> component) {
        this.component = component;
    }

    @Override
    public Iterator<PartPos> getNonEmptyPositions() {
        return Iterators.forArray();
    }

    @Override
    public Iterator<PartPos> getPositions(T instance, M matchFlags) {
        return Iterators.forArray();
    }

    @Override
    public void addPosition(T instance, PrioritizedPartPos pos) {

    }

    @Override
    public void removePosition(T instance, PrioritizedPartPos pos) {

    }

    @Override
    public long getQuantity(T instance) {
        return 0;
    }

    @Override
    public boolean contains(T instance) {
        return false;
    }

    @Override
    public boolean contains(T instance, M matchCondition) {
        return false;
    }

    @Override
    public int count(T instance, M matchCondition) {
        return 0;
    }

    @Override
    public IngredientComponent<T, M> getComponent() {
        return this.component;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Iterator<T> iterator(T instance, M matchCondition) {
        return Iterators.forArray();
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.forArray();
    }
}
