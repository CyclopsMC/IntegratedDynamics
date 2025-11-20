package org.cyclops.integrateddynamics.core.network;

import it.unimi.dsi.fastutil.ints.AbstractInt2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.datastructure.MultitransformIterator;
import org.cyclops.cyclopscore.ingredient.collection.*;
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
    private final AbstractInt2ObjectSortedMap<IIngredientCollapsedCollectionMutable<T, M>> ingredientInstancesCollapsed;
    private final AbstractInt2ObjectSortedMap<IIngredientCollectionMutable<T, M>> ingredientInstancesUncollapsed;

    public IngredientPositionsIndex(IngredientComponent<T, M> component) {
        this.component = component;
        this.prioritizedPositionsMap = new Int2ObjectAVLTreeMap<>();
        this.ingredientInstancesCollapsed = new Int2ObjectAVLTreeMap<>();
        this.ingredientInstancesUncollapsed = new Int2ObjectAVLTreeMap<>();
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
        M finalMatchFlags = matchFlags;

        return this.prioritizedPositionsMap.values()
                .stream()
                .flatMap(ingredientCollection -> ingredientCollection.getAll(getPrototype(instance), finalMatchFlags).stream())
                .flatMap(Collection::stream)
                .distinct()
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
        return this.ingredientInstancesCollapsed.values().stream()
                .mapToLong(ingredients -> ingredients.getQuantity(instance))
                .sum();
    }

    @Override
    public IngredientComponent<T, M> getComponent() {
        return this.component;
    }

    @Override
    public int size() {
        return this.ingredientInstancesCollapsed.values().stream()
                .mapToInt(IIngredientCollapsedCollectionMutable::size)
                .sum();
    }

    @Override
    public boolean contains(T instance) {
        return this.ingredientInstancesCollapsed.values().stream()
                .anyMatch(ingredients -> ingredients.contains(instance));
    }

    @Override
    public boolean contains(T instance, M matchCondition) {
        return this.ingredientInstancesCollapsed.values().stream()
                .anyMatch(ingredients -> ingredients.contains(instance, matchCondition));
    }

    @Override
    public int count(T instance, M matchCondition) {
        return this.ingredientInstancesCollapsed.values().stream()
                .mapToInt(ingredients -> ingredients.count(instance, matchCondition))
                .sum();
    }

    @Override
    public Iterator<T> iterator(T instance, M matchCondition) {
        return new MultitransformIterator<>(this.ingredientInstancesCollapsed.values().iterator(),
                ingredients -> ingredients.iterator(instance, matchCondition));
    }

    @Override
    public Iterator<T> iterator() {
        return new MultitransformIterator<>(this.ingredientInstancesCollapsed.values().iterator(),
                IIngredientCollapsedCollectionMutable::iterator);
    }

    @Override
    public Iterator<T> iteratorUncollapsed() {
        return new MultitransformIterator<>(this.ingredientInstancesUncollapsed.values().iterator(),
                IIngredientCollectionMutable::iterator);
    }

    public void removeAll(PrioritizedPartPos pos, Iterable<? extends T> instances) {
        IIngredientCollapsedCollectionMutable<T, M> ingredientsCollapsed = this.ingredientInstancesCollapsed.get(getInternalPriority(pos));
        if (ingredientsCollapsed != null) {
            ingredientsCollapsed.removeAll(instances);
            if (ingredientsCollapsed.isEmpty()) {
                this.ingredientInstancesCollapsed.remove(getInternalPriority(pos));
            }
        }

        IIngredientCollectionMutable<T, M> ingredientsUncollapsed = this.ingredientInstancesUncollapsed.get(getInternalPriority(pos));
        if (ingredientsUncollapsed != null) {
            ingredientsUncollapsed.removeAll(instances);
            if (ingredientsUncollapsed.isEmpty()) {
                this.ingredientInstancesUncollapsed.remove(getInternalPriority(pos));
            }
        }
    }

    public void addAll(PrioritizedPartPos pos, Iterable<? extends T> instances) {
        IIngredientCollapsedCollectionMutable<T, M> ingredientsCollapsed = this.ingredientInstancesCollapsed.get(getInternalPriority(pos));
        if (ingredientsCollapsed == null) {
            ingredientsCollapsed = IngredientCollectionHelpers.createCollapsedCollection(component);
            this.ingredientInstancesCollapsed.put(getInternalPriority(pos), ingredientsCollapsed);
        }
        ingredientsCollapsed.addAll(instances);

        IIngredientCollectionMutable<T, M> ingredientsUncollapsed = this.ingredientInstancesUncollapsed.get(getInternalPriority(pos));
        if (ingredientsUncollapsed == null) {
            ingredientsUncollapsed = createUncollapsedCollection(component);
            this.ingredientInstancesUncollapsed.put(getInternalPriority(pos), ingredientsUncollapsed);
        }
        ingredientsUncollapsed.addAll(instances);
    }

    // TODO: move to CyclopsCore IngredientCollectionHelpers in next major
    public static <T, M> IIngredientCollectionMutable<T, M> createUncollapsedCollection(IngredientComponent<T, M> ingredientComponent) {
        if (ingredientComponent.getCategoryTypes().size() == 1) {
            return new IngredientArrayList<>(ingredientComponent);
        }
        return new IngredientCollectionSingleClassified<>(
                ingredientComponent,
                () -> new IngredientArrayList<>(ingredientComponent),
                ingredientComponent.getCategoryTypes().get(0));
    }
}
