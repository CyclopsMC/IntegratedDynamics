package org.cyclops.integrateddynamics.api.ingredient;

import org.cyclops.cyclopscore.ingredient.collection.IIngredientCollection;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PrioritizedPartPos;

import java.util.Iterator;

/**
 * An index that maps ingredients to positions that contain that instance.
 *
 * @param <T> An instance type.
 * @param <M> The matching condition parameter.
 */
public interface IIngredientPositionsIndex<T, M> extends IIngredientCollection<T, M> {

    /**
     * @return Get all positions that are not empty.
     */
    public Iterator<PartPos> getNonEmptyPositions();

    /**
     * Get all positions that have an instance that contain the given instance.
     * @param instance An instance to match.
     * @param matchFlags Instance match conditions.
     * @return The positions.
     */
    public Iterator<PartPos> getPositions(T instance, M matchFlags);

    /**
     * Indicate that the given position contains the given position.
     * @param instance An instance.
     * @param pos A position.
     */
    public void addPosition(T instance, PrioritizedPartPos pos);

    /**
     * Indicate that the given instance is removed from the given position.
     *
     * This will not necessarily remove the given position,
     * only if the total instance quantity becomes zero.
     *
     * @param instance An instance.
     * @param pos A position.
     */
    public void removePosition(T instance, PrioritizedPartPos pos);

    /**
     * Get the available quantity for the given instance.
     * @param instance An instance to match.
     * @return The indexed quantity.
     */
    public long getQuantity(T instance);

}
