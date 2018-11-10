package org.cyclops.integrateddynamics.api.network;

import net.minecraft.util.EnumFacing;
import org.cyclops.integrateddynamics.api.part.PartPos;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A network that can hold prioritized positions.
 * @author rubensworks
 */
public interface IPositionedAddonsNetwork {

    /**
     * @param channel The channel id.
     * @return The stored positions, sorted by priority.
     */
    public Collection<PrioritizedPartPos> getPositions(int channel);

    /**
     * @return All stored positions, order is undefined.
     */
    public Collection<PrioritizedPartPos> getPositions();

    /**
     * Get an iterator over the positions in the given channel.
     *
     * This can return a cloned state of the internal iterator.
     * In some cases, it can be useful to commit the new state of the iterator
     * by calling {@link IPositionedAddonsNetwork#setPositionIterator(PositionsIterator, int)}.
     *
     * @param channel The channel id.
     * @return A positions iterator.
     */
    public PositionsIterator getPositionIterator(int channel);

    /**
     * Set the iterator over the positions in the given channel.
     * @param iterator The iterator, null if the internal network iteration order should be used.
     * @param channel The channel id.
     */
    public void setPositionIterator(@Nullable PositionsIterator iterator, int channel);

    /**
     * Create a new iterator over the positions in the given channel.
     * @param channel The channel id.
     * @return A new positions iterator.
     */
    public PositionsIterator createPositionIterator(int channel);

    /**
     * Must be called for every position iterator that was created.
     * This should not be called if the iterator was created using
     * {@link IPositionedAddonsNetwork#createPositionIterator(int)}.
     * @param positionsIterator A positions iterator.
     */
    public void onPositionIteratorCreated(PositionsIterator positionsIterator);

    /**
     * Add the given position.
     * @param pos The position.
     * @param priority The priority.
     * @param channel The channel id.
     * @return If the position was added, otherwise it was already present.
     */
    public boolean addPosition(PartPos pos, int priority, int channel);

    /**
     * Remove the given position.
     * @param pos The position.
     */
    public void removePosition(PartPos pos);

    /**
     * Check if the given position is disabled.
     * @param pos The position.
     * @return If it is disabled.
     */
    public boolean isPositionDisabled(PartPos pos);

    /**
     * Disable a position.
     * @param pos The position.
     */
    public void disablePosition(PartPos pos);

    /**
     * Enable a position.
     * @param pos The position.
     */
    public void enablePosition(PartPos pos);

    public static class PrioritizedPartPos implements Comparable<PrioritizedPartPos> {
        private final PartPos partPos;
        private final int priority;

        private PrioritizedPartPos(PartPos partPos, int priority) {
            this.partPos = partPos;
            this.priority = priority;
        }

        @Override
        public int compareTo(PrioritizedPartPos o) {
            int compPriority = -Integer.compare(this.getPriority(), o.getPriority());
            if (compPriority == 0) {
                int compPos = this.getPartPos().getPos().compareTo(o.getPartPos().getPos());
                if (compPos == 0) {
                    EnumFacing thisSide = this.getPartPos().getSide();
                    EnumFacing otherSide = o.getPartPos().getSide();
                    return thisSide == null ? -1 : (otherSide == null ? 1 : thisSide.compareTo(otherSide));
                }
                return compPos;
            }
            return compPriority;
        }

        public static PrioritizedPartPos of(PartPos pos, int priority) {
            return new PrioritizedPartPos(pos, priority);
        }

        public PartPos getPartPos() {
            return partPos;
        }

        public int getPriority() {
            return priority;
        }
    }

    public static class PositionsIterator implements Iterator<PrioritizedPartPos> {

        private boolean valid;
        private final Collection<PrioritizedPartPos> collection;
        private final Iterator<PrioritizedPartPos> it;
        private int steps;
        private final Set<PositionsIterator> children;
        private final IPositionedAddonsNetwork positionedAddonsNetwork;

        private PrioritizedPartPos[] toAppend = new PrioritizedPartPos[0];
        private int toAppendStep = 0;

        public PositionsIterator(Collection<PrioritizedPartPos> collection,
                                 IPositionedAddonsNetwork positionedAddonsNetwork) {
            this.valid = true;
            this.collection = collection;
            this.it = this.collection.iterator();
            this.steps = 0;
            this.children = Collections.newSetFromMap(new WeakHashMap<>());
            this.positionedAddonsNetwork = positionedAddonsNetwork;
        }

        public void invalidate() {
            this.valid = false;
            this.children.forEach(PositionsIterator::invalidate);
        }

        @Override
        public boolean hasNext() {
            return valid && (it.hasNext() || toAppendStep < toAppend.length);
        }

        @Override
        public PrioritizedPartPos next() {
            steps++;
            if (steps >= this.collection.size()) {
                // This can occur after looping over the append steps.
                // This will make sure that cloning will still work properly.
                steps = steps % this.collection.size();
            }
            return it.hasNext() ? it.next() : toAppend[toAppendStep++];
        }

        protected void setToAppend(PrioritizedPartPos[] toAppend) {
            this.toAppend = toAppend;
        }

        /**
         * Clone this iterator.
         * This will also make sure that the skipped steps are appended in-order at the end of the iterator.
         * @return A cloned iterator.
         */
        public PositionsIterator cloneState() {
            PositionsIterator child = new PositionsIterator(this.collection, positionedAddonsNetwork);
            positionedAddonsNetwork.onPositionIteratorCreated(child);
            this.children.add(child);
            PrioritizedPartPos[] toAppend = new PrioritizedPartPos[steps];
            for (int step = 0; step < steps; step++) {
                toAppend[step] = child.next();
            }
            child.setToAppend(toAppend);
            return child;
        }
    }

}
