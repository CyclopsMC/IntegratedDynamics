package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.Iterators;
import org.cyclops.integrateddynamics.api.network.IPartPosIteratorHandler;
import org.cyclops.integrateddynamics.api.part.PartPos;

import java.util.Iterator;
import java.util.function.Supplier;

/**
 * An {@link IPartPosIteratorHandler} that maintains the state of an iterator
 * and achieves round-robin iteration.
 * @author rubensworks
 */
public class PartPosIteratorHandlerRoundRobin implements IPartPosIteratorHandler {

    private int offset;

    public PartPosIteratorHandlerRoundRobin(int offset) {
        this.offset = offset;
    }

    public PartPosIteratorHandlerRoundRobin() {
        this(0);
    }

    @Override
    public Iterator<PartPos> handleIterator(Supplier<Iterator<PartPos>> iteratorSupplier, int channel) {
        Iterator<PartPos> it = iteratorSupplier.get();

        // Offset the iterator
        int advanced = Iterators.advance(it, offset);
        if (offset >= advanced && !it.hasNext()) {
            // Restart the iterator if the offset reached the end.
            offset = Math.max(0, offset - advanced);
            it = iteratorSupplier.get();
        }

        // Wrap the iterator in an iterator that will increase the offset of this handler for each `next` call.
        // If the initial offset was >0, it will allow the iterator to be reset
        // and iterate over the reset iterator `offset` times.
        return new CountingIterator(this, it, offset, iteratorSupplier);
    }

    @Override
    public IPartPosIteratorHandler clone() {
        return new PartPosIteratorHandlerRoundRobin(this.offset);
    }

    public static class CountingIterator implements Iterator<PartPos> {

        private final PartPosIteratorHandlerRoundRobin handler;
        private Iterator<PartPos> innerIt;
        private final Supplier<Iterator<PartPos>> iteratorSupplier;

        private boolean countdownAllowedRemaining;
        private int allowedRemaining;

        public CountingIterator(PartPosIteratorHandlerRoundRobin handler, Iterator<PartPos> innerIt,
                                int initialOffset, Supplier<Iterator<PartPos>> iteratorSupplier) {
            this.handler = handler;
            this.innerIt = innerIt;
            this.iteratorSupplier = iteratorSupplier;

            this.countdownAllowedRemaining = false;
            this.allowedRemaining = initialOffset;
        }

        @Override
        public boolean hasNext() {
            if (innerIt.hasNext()) {
                return true;
            } else if (!countdownAllowedRemaining) {
                countdownAllowedRemaining = true;
                if (allowedRemaining > 0) {
                    innerIt = iteratorSupplier.get();
                    return innerIt.hasNext();
                }
            }
            return false;
        }

        @Override
        public PartPos next() {
            PartPos next = innerIt.next();

            // Handle offset
            handler.offset++;
            if (countdownAllowedRemaining) {
                allowedRemaining--;
                if (allowedRemaining <= 0) {
                    // Set to empty iterator if we are not allowed to progress anymore.
                    innerIt = Iterators.forArray();
                }

            }

            return next;
        }
    }
}
