package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.Sets;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.datastructure.DistinctIterator;
import org.cyclops.cyclopscore.ingredient.collection.IIngredientMapMutable;
import org.cyclops.cyclopscore.ingredient.collection.IngredientCollectionMutableWrapper;
import org.cyclops.cyclopscore.ingredient.collection.IngredientCollectionPrototypeMap;
import org.cyclops.cyclopscore.ingredient.collection.IngredientHashMap;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientPositionsIndex;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PrioritizedPartPos;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * An index that maps ingredients to positions that contain that instance.
 * @param <T> An instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public class IngredientPositionsIndex<T, M> extends IngredientCollectionMutableWrapper<T, M, IngredientCollectionPrototypeMap<T, M>>
        implements IIngredientPositionsIndex<T, M> {

    private final IIngredientMapMutable<T, M, TreeSet<PrioritizedPartPos>> positionsMap;

    public IngredientPositionsIndex(IngredientComponent<T, M> component) {
        super(new IngredientCollectionPrototypeMap<>(component, false));
        this.positionsMap = new IngredientHashMap<>(component);
    }

    protected T getPrototype(T instance) {
        return this.positionsMap.getComponent().getMatcher().withQuantity(instance, 1);
    }

    @Override
    public Iterator<PartPos> getNonEmptyPositions() {
        return getPositions(getComponent().getMatcher().getEmptyInstance(), getComponent().getMatcher().getAnyMatchCondition());
    }

    @Override
    public Iterator<PartPos> getPositions(T instance, M matchFlags) {
        return new DistinctIterator<>(this.positionsMap.getAll(getPrototype(instance), matchFlags)
                .stream()
                .flatMap(Collection::stream)
                .map(PrioritizedPartPos::getPartPos)
                .iterator());
    }

    @Override
    public void addPosition(T instance, PrioritizedPartPos pos) {
        T prototype = getPrototype(instance);
        TreeSet<PrioritizedPartPos> set = this.positionsMap.get(prototype);
        if (set == null) {
            set = Sets.newTreeSet();
            this.positionsMap.put(prototype, set);
        }
        set.add(pos);
    }

    @Override
    public void removePosition(T instance, PrioritizedPartPos pos) {
        T prototype = getPrototype(instance);
        TreeSet<PrioritizedPartPos> set = this.positionsMap.get(prototype);
        if (set != null) {
            set.remove(pos);
            if (set.isEmpty()) {
                this.positionsMap.remove(prototype);
            }
        }
    }

    @Override
    public long getQuantity(T instance) {
        return getInnerCollection().getQuantity(instance);
    }

}
