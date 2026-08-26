package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.AbstractInt2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.datastructure.MultitransformIterator;
import org.cyclops.cyclopscore.ingredient.collection.IIngredientCollapsedCollectionMutable;
import org.cyclops.cyclopscore.ingredient.collection.IIngredientMapMutable;
import org.cyclops.cyclopscore.ingredient.collection.IngredientCollectionHelpers;
import org.cyclops.cyclopscore.ingredient.collection.IngredientHashMap;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientPositionsIndex;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PrioritizedPartPos;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

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
    private final AbstractInt2ObjectSortedMap<IIngredientCollapsedCollectionMutable<T, M>> ingredientInstances;

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
        // Since we store ingredients by prototype in ingredientCollection,
        // we can make the match flags more precise,
        // and possibly improve performance of the lookup operation.
        IIngredientMatcher<T, M> matcher = getComponent().getMatcher();
        if (matcher.getExactMatchNoQuantityCondition().equals(matchFlags)) {
            matchFlags = matcher.getExactMatchCondition();
        }

        if (matcher.getExactMatchCondition().equals(matchFlags)) {
            // Fast path: instances are stored by prototype, and the position maps hash their keys by exact equality,
            // so a single hash-based lookup per priority level suffices.
            // (IIngredientMap#getAll would instead construct an intermediate key set
            //  that is pre-allocated for the size of the whole map.)
            T prototype = getPrototype(instance);
            return iteratePrioritized(positionsMap -> {
                ObjectOpenHashSet<PartPos> positions = positionsMap.get(prototype);
                return positions == null ? Collections.emptyIterator() : positions.iterator();
            });
        }

        T prototype = getPrototype(instance);
        M finalMatchFlags = matchFlags;
        return iteratePrioritized(positionsMap -> Iterators.concat(Iterators.transform(
                positionsMap.getAll(prototype, finalMatchFlags).iterator(), Collection::iterator)));
    }

    /**
     * Lazily iterate over the positions of all priority levels, in priority order, without duplicates.
     *
     * Iteration is lazy because callers commonly stop iterating
     * as soon as they have found a usable position.
     *
     * @param positionsGetter A callback for iterating over the matching positions within one priority level.
     * @return An iterator over all matching positions.
     */
    protected Iterator<PartPos> iteratePrioritized(Function<IIngredientMapMutable<T, M, ObjectOpenHashSet<PartPos>>, Iterator<PartPos>> positionsGetter) {
        return distinct(Iterators.concat(Iterators.transform(
                this.prioritizedPositionsMap.values().iterator(), positionsGetter::apply)));
    }

    /**
     * Filter out duplicate positions, as the same position can hold multiple instances.
     *
     * @param positions An iterator over positions.
     * @return An iterator over distinct positions.
     */
    protected Iterator<PartPos> distinct(Iterator<PartPos> positions) {
        Set<PartPos> seenPositions = Sets.newHashSet();
        return Iterators.filter(positions, seenPositions::add);
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
                .mapToInt(IIngredientCollapsedCollectionMutable::size)
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
                IIngredientCollapsedCollectionMutable::iterator);
    }

    public void removeAll(PrioritizedPartPos pos, Iterable<? extends T> instances) {
        IIngredientCollapsedCollectionMutable<T, M> ingredients = this.ingredientInstances.get(getInternalPriority(pos));
        if (ingredients != null) {
            ingredients.removeAll(instances);
            if (ingredients.isEmpty()) {
                this.ingredientInstances.remove(getInternalPriority(pos));
            }
        }
    }

    public void addAll(PrioritizedPartPos pos, Iterable<? extends T> instances) {
        IIngredientCollapsedCollectionMutable<T, M> ingredients = this.ingredientInstances.get(getInternalPriority(pos));
        if (ingredients == null) {
            ingredients = IngredientCollectionHelpers.createCollapsedCollection(component);
            this.ingredientInstances.put(getInternalPriority(pos), ingredients);
        }
        ingredients.addAll(instances);
    }
}
