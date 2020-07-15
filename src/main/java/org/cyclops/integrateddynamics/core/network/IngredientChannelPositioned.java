package org.cyclops.integrateddynamics.core.network;

import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.api.part.PartPos;

import javax.annotation.Nonnull;
import java.util.Iterator;

/**
 * An ingredient channel that naively iterates over all positions in the network.
 *
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 */
public class IngredientChannelPositioned<T, M> extends IngredientChannelAdapter<T, M> {

    public IngredientChannelPositioned(PositionedAddonsNetworkIngredients<T, M> network, int channel) {
        super(network, channel);
    }

    @Override
    protected Iterator<PartPos> getNonFullPositions() {
        return getAllPositions();
    }

    @Override
    protected Iterator<PartPos> getAllPositions() {
        this.scheduleObservation();
        return getNetwork().getPositions(getChannel()).iterator();
    }

    @Override
    protected Iterator<PartPos> getNonEmptyPositions() {
        return getAllPositions();
    }

    @Override
    protected Iterator<PartPos> getMatchingPositions(@Nonnull T prototype, M matchFlags) {
        return getAllPositions();
    }

    @Override
    public Iterator<T> iterator() {
        this.scheduleObservation();
        return new PositionedIngredientIterator<>(getNetwork(), getNetwork().getPositions(getChannel()).iterator(),
                getComponent().getMatcher().getEmptyInstance(), getComponent().getMatcher().getAnyMatchCondition());
    }

    @Override
    public Iterator<T> iterator(@Nonnull T prototype, M matchFlags) {
        this.scheduleObservation();
        return new PositionedIngredientIterator<>(getNetwork(), getNetwork().getPositions(getChannel()).iterator(),
                prototype, matchFlags);
    }

    public static class PositionedIngredientIterator<T, M> implements Iterator<T> {

        private final IPositionedAddonsNetworkIngredients<T, M> network;
        private final Iterator<PartPos> it;
        private final T prototype;
        private final M matchFlags;
        private Iterator<T> lastPos;

        public PositionedIngredientIterator(IPositionedAddonsNetworkIngredients<T, M> network,
                                            Iterator<PartPos> it, T prototype, M matchFlags) {
            this.network = network;
            this.it = it;
            this.prototype = prototype;
            this.matchFlags = matchFlags;
            prepareLastPos();
        }

        protected void prepareLastPos() {
            do {
                if (it.hasNext()) {
                    PartPos pos = it.next();
                    // Skip if the position is not loaded
                    if (!pos.getPos().isLoaded()) {
                        continue;
                    }
                    this.lastPos = this.network.getPositionedStorage(pos).iterator(prototype, matchFlags);
                } else {
                    this.lastPos = null;
                }
            } while (this.lastPos != null && !this.lastPos.hasNext());
        }

        @Override
        public boolean hasNext() {
            return lastPos != null;
        }

        @Override
        public T next() {
            T next = lastPos.next();
            if (!lastPos.hasNext()) {
                prepareLastPos();
            }
            return next;
        }
    }

}
