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
        private final int priority, channel;

        private PrioritizedPartPos(PartPos partPos, int priority, int channel) {
            this.partPos = partPos;
            this.priority = priority;
            this.channel = channel;
        }

        @Override
        public int compareTo(PrioritizedPartPos o) {
            int compPriority = -Integer.compare(this.getPriority(), o.getPriority());
            if (compPriority == 0) {
                int compPos = this.getPartPos().getPos().compareTo(o.getPartPos().getPos());
                if (compPos == 0) {
                    EnumFacing thisSide = this.getPartPos().getSide();
                    EnumFacing otherSide = o.getPartPos().getSide();
                    return thisSide == otherSide ? Integer.compare(this.getChannel(), o.getChannel()) : (thisSide == null ? -1 : (otherSide == null ? 1 : thisSide.compareTo(otherSide)));
                }
                return compPos;
            }
            return compPriority;
        }

        public static PrioritizedPartPos of(PartPos pos, int priority, int channel) {
            return new PrioritizedPartPos(pos, priority, channel);
        }

        public PartPos getPartPos() {
            return partPos;
        }

        public int getPriority() {
            return priority;
        }

        public int getChannel() {
            return channel;
        }
    }

}
