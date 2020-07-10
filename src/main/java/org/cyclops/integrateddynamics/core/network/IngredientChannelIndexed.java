package org.cyclops.integrateddynamics.core.network;

import org.cyclops.integrateddynamics.api.ingredient.IIngredientPositionsIndex;
import org.cyclops.integrateddynamics.api.part.PartPos;

import javax.annotation.Nonnull;
import java.util.Iterator;

/**
 * An ingredient channel that exploits the network's index.
 *
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 */
public class IngredientChannelIndexed<T, M> extends IngredientChannelAdapter<T, M> {

    private final IIngredientPositionsIndex<T, M> index;

    public IngredientChannelIndexed(PositionedAddonsNetworkIngredients<T, M> network, int channel,
                                    IIngredientPositionsIndex<T, M> index) {
        super(network, channel);
        this.index = index;
    }

    @Override
    protected Iterator<PartPos> getNonFullPositions() {
        this.scheduleObservation();
        return this.getNetwork().getPositions(getChannel()).iterator();
    }

    @Override
    protected Iterator<PartPos> getAllPositions() {
        this.scheduleObservation();
        return this.getNetwork().getPositions(getChannel()).iterator();
    }

    @Override
    protected Iterator<PartPos> getNonEmptyPositions() {
        this.scheduleObservation();
        return this.index.getNonEmptyPositions();
    }

    @Override
    protected Iterator<PartPos> getMatchingPositions(@Nonnull T prototype, M matchFlags) {
        this.scheduleObservation();
        return this.index.getPositions(prototype, matchFlags);
    }

    @Override
    public Iterator<T> iterator() {
        this.scheduleObservation();
        return this.index.iterator();
    }

    @Override
    public Iterator<T> iterator(@Nonnull T prototype, M matchFlags) {
        this.scheduleObservation();
        return this.index.iterator(prototype, matchFlags);
    }

    @Override
    protected boolean canExtract(T extractedSimulated) {
        return index.getQuantity(extractedSimulated) >= getComponent().getMatcher().getQuantity(extractedSimulated);
    }
}
