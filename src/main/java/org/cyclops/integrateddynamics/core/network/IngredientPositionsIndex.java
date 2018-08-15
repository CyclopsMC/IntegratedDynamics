package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.Sets;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.datastructure.DistinctIterator;
import org.cyclops.cyclopscore.datastructure.MultitransformIterator;
import org.cyclops.cyclopscore.ingredient.collection.IIngredientCollectionMutable;
import org.cyclops.cyclopscore.ingredient.collection.IIngredientMapMutable;
import org.cyclops.cyclopscore.ingredient.collection.IngredientCollectionMutableWrapper;
import org.cyclops.cyclopscore.ingredient.collection.IngredientCollectionPrototypeMap;
import org.cyclops.cyclopscore.ingredient.collection.IngredientHashMap;
import org.cyclops.integrateddynamics.api.part.PartPos;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * An index that maps ingredients to positions that contain that instance.
 * @author rubensworks
 */
public class IngredientPositionsIndex<T, M> extends IngredientCollectionMutableWrapper<T, M, IIngredientCollectionMutable<T, M>> {

    private final IIngredientMapMutable<T, M, TreeSet<PartPos>> positionsMap;

    public IngredientPositionsIndex(IngredientComponent<T, M> component) {
        super(new IngredientCollectionPrototypeMap<>(component, false));
        this.positionsMap = new IngredientHashMap<>(component);
    }

    protected T getPrototype(T instance) {
        return this.positionsMap.getComponent().getMatcher().withQuantity(instance, 1);
    }

    public Iterator<PartPos> getNonEmptyPositions() {
        return getPositions(getComponent().getMatcher().getEmptyInstance(), getComponent().getMatcher().getAnyMatchCondition());
    }

    public Iterator<PartPos> getPositions(T instance, M matchFlags) {
        return new DistinctIterator<>(MultitransformIterator.flattenIterableIterator(
                this.positionsMap.getAll(getPrototype(instance), matchFlags).iterator()));
    }

    public void addPosition(T instance, PartPos pos) {
        T prototype = getPrototype(instance);
        TreeSet<PartPos> set = this.positionsMap.get(prototype);
        if (set == null) {
            set = Sets.newTreeSet();
            this.positionsMap.put(prototype, set);
        }
        set.add(pos);
    }

    public void removePosition(T instance, PartPos pos) {
        T prototype = getPrototype(instance);
        TreeSet<PartPos> set = this.positionsMap.get(prototype);
        if (set != null) {
            set.remove(pos);
            if (set.isEmpty()) {
                this.positionsMap.remove(prototype);
            }
        }
    }

}
