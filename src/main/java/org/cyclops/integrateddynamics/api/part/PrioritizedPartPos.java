package org.cyclops.integrateddynamics.api.part;

import net.minecraft.util.EnumFacing;

/**
 * A wrapper around {@link PartPos} that also holds a priority.
 *
 * This is useful for automatically providing sorted collections.
 *
 * @author rubensworks
 */
public class PrioritizedPartPos implements Comparable<PrioritizedPartPos> {
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

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PrioritizedPartPos && compareTo((PrioritizedPartPos) obj) == 0;
    }

    @Override
    public int hashCode() {
        return getPartPos().hashCode() + getPriority() << 1;
    }
}
