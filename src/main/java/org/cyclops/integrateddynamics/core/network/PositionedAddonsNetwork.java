package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.Getter;
import lombok.Setter;
import org.cyclops.integrateddynamics.api.network.IChanneledNetwork;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.api.part.PartPos;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A network that can hold prioritized positions.
 * @author rubensworks
 */
public class PositionedAddonsNetwork implements IPositionedAddonsNetwork {

    @Getter
    @Setter
    private INetwork network;
    private final TIntObjectMap<Set<PrioritizedPartPos>> positions = new TIntObjectHashMap<>();
    private final Set<PartPos> disabledPositions = Sets.newHashSet();

    @Override
    public Collection<PrioritizedPartPos> getPositions(int channel) {
        if (channel == IChanneledNetwork.WILDCARD_CHANNEL) {
            return getPositions();
        }
        Set<PrioritizedPartPos> positions = this.positions.get(channel);
        if (positions == null) {
            return Collections.emptySet();
        }
        return ImmutableSet.copyOf(positions);
    }

    @Override
    public Collection<PrioritizedPartPos> getPositions() {
        List<PrioritizedPartPos> allPositions = Lists.newArrayList();
        for (Set<PrioritizedPartPos> positions : this.positions.valueCollection()) {
            allPositions.addAll(positions);
        }
        return allPositions;
    }

    @Override
    public boolean addPosition(PartPos pos, int priority, int channel) {
        Set<PrioritizedPartPos> positions = this.positions.get(channel);
        if (positions == null) {
            positions = Sets.newTreeSet();
            this.positions.put(channel, positions);
        }
        return positions.add(PrioritizedPartPos.of(pos, priority));
    }

    @Override
    public void removePosition(PartPos pos) {
        for (Set<PrioritizedPartPos> positions : this.positions.valueCollection()) {
            positions.removeIf(prioritizedPartPos -> prioritizedPartPos.getPartPos().equals(pos));
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
