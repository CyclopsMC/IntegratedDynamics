package org.cyclops.integrateddynamics.core.network;

import org.cyclops.integrateddynamics.api.ingredient.IIngredientPositionsIndex;

import java.util.Iterator;

/**
 * An uncollapsed ingredient channel that exploits the network's index.
 *
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 */
public class IngredientChannelIndexedUncollapsed<T, M> extends IngredientChannelIndexed<T, M> {
    public IngredientChannelIndexedUncollapsed(PositionedAddonsNetworkIngredients<T, M> network, int channel, IIngredientPositionsIndex<T, M> index) {
        super(network, channel, index);
    }

    @Override
    public Iterator<T> iterator() {
        this.scheduleObservation();
        return this.getIndex().iteratorUncollapsed();
    }
}
