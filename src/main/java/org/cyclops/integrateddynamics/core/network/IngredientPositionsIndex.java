package org.cyclops.integrateddynamics.core.network;

import it.unimi.dsi.fastutil.ints.AbstractInt2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.datastructure.MultitransformIterator;
import org.cyclops.cyclopscore.ingredient.collection.IIngredientMapMutable;
import org.cyclops.cyclopscore.ingredient.collection.IngredientCollectionPrototypeMap;
import org.cyclops.cyclopscore.ingredient.collection.IngredientHashMap;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientPositionsIndex;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PrioritizedPartPos;

import java.util.Collection;
import java.util.Iterator;

/**
 * An index that maps ingredients to positions that contain that instance.
 *
 * Positions are stored together with their priorities.
 * This makes it possible for instances of this class to guarantee that all returned iterators maintain priority order.
 *
 * @param <T> An instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public class IngredientPositionsIndex<T, M> implements IIngredientPositionsIndex<T, M> {

    private final IngredientComponent<T, M> component;
    private final AbstractInt2ObjectSortedMap<IIngredientMapMutable<T, M, ObjectOpenHashSet<PartPos>>> prioritizedPositionsMap;
    private final AbstractInt2ObjectSortedMap<IngredientCollectionPrototypeMap<T, M>> ingredientInstances;

    public IngredientPositionsIndex(IngredientComponent<T, M> component) {
        this.component = component;
        this.prioritizedPositionsMap = new Int2ObjectAVLTreeMap<>();
        this.ingredientInstances = new Int2ObjectAVLTreeMap<>();
    }

    protected T getPrototype(T instance) {
        return this.getComponent().getMatcher().withQuantity(instance, 1);
    }

    protected int getInternalPriority(PrioritizedPartPos pos) {
        // We need to do this as we want higher values to be returned first within the iterator
        return -pos.getPriority();
    }

    @Override
    public Iterator<PartPos> getNonEmptyPositions() {
        return getPositions(getComponent().getMatcher().getEmptyInstance(), getComponent().getMatcher().getAnyMatchCondition());
    }

    @Override
    public Iterator<PartPos> getPositions(T instance, M matchFlags) {
        return this.prioritizedPositionsMap.values()
                .stream()
                .flatMap(ingredientCollection -> ingredientCollection.getAll(getPrototype(instance), matchFlags).stream())
                .flatMap(Collection::stream)
                .iterator();
    }

    @Override
    public void addPosition(T instance, PrioritizedPartPos pos) {
        IIngredientMapMutable<T, M, ObjectOpenHashSet<PartPos>> positionsMap = this.prioritizedPositionsMap.get(getInternalPriority(pos));
        if (positionsMap == null) {
            positionsMap = new IngredientHashMap<>(getComponent());
            this.prioritizedPositionsMap.put(getInternalPriority(pos), positionsMap);
        }

        T prototype = getPrototype(instance);
        ObjectOpenHashSet<PartPos> set = positionsMap.get(prototype);
        if (set == null) {
            set = new ObjectOpenHashSet<>();
            positionsMap.put(prototype, set);
        }

        set.add(pos.getPartPos());
    }

    @Override
    public void removePosition(T instance, PrioritizedPartPos pos) {
        IIngredientMapMutable<T, M, ObjectOpenHashSet<PartPos>> positionsMap = this.prioritizedPositionsMap.get(getInternalPriority(pos));
        if (positionsMap != null) {
            T prototype = getPrototype(instance);
            ObjectOpenHashSet<PartPos> set = positionsMap.get(prototype);
            if (set != null) {
                set.remove(pos.getPartPos());
                if (set.isEmpty()) {
                    positionsMap.remove(prototype);
                    if (positionsMap.isEmpty()) {
                        this.prioritizedPositionsMap.remove(getInternalPriority(pos));
                    }
                }
            }
        }
    }

    @Override
    public long getQuantity(T instance) {
        return this.ingredientInstances.values().stream()
                .mapToLong(ingredients -> ingredients.getQuantity(instance))
                .sum();
    }

    @Override
    public IngredientComponent<T, M> getComponent() {
        return this.component;
    }

    @Override
    public int size() {
        return this.ingredientInstances.values().stream()
                .mapToInt(IngredientCollectionPrototypeMap::size)
                .sum();
    }

    @Override
    public boolean contains(T instance) {
        return this.ingredientInstances.values().stream()
                .anyMatch(ingredients -> ingredients.contains(instance));
    }

    @Override
    public boolean contains(T instance, M matchCondition) {
        return this.ingredientInstances.values().stream()
                .anyMatch(ingredients -> ingredients.contains(instance, matchCondition));
    }

    @Override
    public int count(T instance, M matchCondition) {
        return this.ingredientInstances.values().stream()
                .mapToInt(ingredients -> ingredients.count(instance, matchCondition))
                .sum();
    }

    @Override
    public Iterator<T> iterator(T instance, M matchCondition) {
        return new MultitransformIterator<>(this.ingredientInstances.values().iterator(),
                ingredients -> ingredients.iterator(instance, matchCondition));
    }

    @Override
    public Iterator<T> iterator() {
        return new MultitransformIterator<>(this.ingredientInstances.values().iterator(),
                IngredientCollectionPrototypeMap::iterator);
    }

    public void removeAll(PrioritizedPartPos pos, Iterable<? extends T> instances) {
        IngredientCollectionPrototypeMap<T, M> ingredients = this.ingredientInstances.get(getInternalPriority(pos));
        if (ingredients != null) {
            ingredients.removeAll(instances);
            if (ingredients.isEmpty()) {
                this.ingredientInstances.remove(getInternalPriority(pos));
            }
        }
    }

    public void addAll(PrioritizedPartPos pos, Iterable<? extends T> instances) {
        IngredientCollectionPrototypeMap<T, M> ingredients = this.ingredientInstances.get(getInternalPriority(pos));
        if (ingredients == null) {
            ingredients = new IngredientCollectionPrototypeMap<>(component, false);
            this.ingredientInstances.put(getInternalPriority(pos), ingredients);
        }
        ingredients.addAll(instances);
    }
}
