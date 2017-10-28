package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.api.part.PartPos;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * A network that can hold prioritized positions.
 * @author rubensworks
 */
public class PositionedAddonsNetwork implements IPositionedAddonsNetwork {

    @Getter
    @Setter
    private INetwork network;
    private final TreeSet<PrioritizedPartPos> positions = Sets.newTreeSet();
    private final Set<PartPos> disabledPositions = Sets.newHashSet();

    @Override
    public Set<PrioritizedPartPos> getPositions() {
        return ImmutableSet.copyOf(positions);
    }

    @Override
    public boolean addPosition(PartPos pos, int priority, int channel) {
        return positions.add(PrioritizedPartPos.of(pos, priority, channel));
    }

    @Override
    public void removePosition(PartPos pos) {
        Iterator<PrioritizedPartPos> it = positions.iterator();
        while (it.hasNext()) {
            if (it.next().getPartPos().equals(pos)) {
                it.remove();
            }
        }
    }

    @Override
    public boolean isPositionDisabled(PartPos pos) {
        return disabledPositions.contains(pos);
    }

    @Override
    public void disablePosition(PartPos pos) {
        disabledPositions.add(pos);
    }

    @Override
    public void enablePosition(PartPos pos) {
        disabledPositions.remove(pos);
    }
}
