package org.cyclops.integrateddynamics.api.network;

import net.minecraft.util.EnumFacing;
import org.cyclops.integrateddynamics.api.part.PartPos;

import java.util.Set;

/**
 * A network that can hold prioritized positions.
 * @author rubensworks
 */
public interface IPositionedAddonsNetwork {

    /**
     * @return The stored positions, sorted by priority.
     */
    public Set<PrioritizedPartPos> getPositions();

    /**
     * Add the given position.
     * @param pos The position.
     * @param priority The priority.
     * @return If the position was added, otherwise it was already present.
     */
    public boolean addPosition(PartPos pos, int priority);

    /**
     * Remove the given position.
     * @param pos The position.
     */
    public void removePosition(PartPos pos);

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
                    return thisSide == otherSide ? 0 : (thisSide == null ? -1 : (otherSide == null ? 1 : thisSide.compareTo(otherSide)));
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

}
