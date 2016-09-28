package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.EnumFacing;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.PartPos;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * A network that can hold prioritized positions.
 * @author rubensworks
 */
public class PositionedAddonsNetwork {

    @Getter
    @Setter
    private INetwork network;
    private Set<PartPos> positions = Sets.newHashSet();
    private TreeSet<PrioritizedPartPos> positionsSorted = Sets.newTreeSet();

    protected Set<PartPos> getPositions() {
        return Collections.unmodifiableSet(positions);
    }

    protected Set<PrioritizedPartPos> getPositionsSorted() {
        return ImmutableSet.copyOf(positionsSorted);
    }

    protected boolean addPosition(PartPos pos, int priority) {
        boolean notContained = positions.add(pos);
        if (notContained) {
            positionsSorted.add(PrioritizedPartPos.of(pos, priority));
        }
        return notContained;
    }

    protected void removePosition(PartPos pos) {
        positions.remove(pos);
        Iterator<PrioritizedPartPos> it = positionsSorted.iterator();
        while (it.hasNext()) {
            if (it.next().getPartPos().equals(pos)) {
                it.remove();
            }
        }
    }

    @Data(staticConstructor = "of")
    public static class PrioritizedPartPos implements Comparable<PrioritizedPartPos> {
        private final PartPos partPos;
        private final int priority;

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
    }
}
