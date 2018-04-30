package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
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

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

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

    private final TIntObjectMap<PositionsIterator> positionsIterators = new TIntObjectHashMap<>();
    private final Set<PositionsIterator> createdIterators = Collections.newSetFromMap(new WeakHashMap<>());

    @Override
    public Collection<PrioritizedPartPos> getPositions(int channel) {
        if (channel == IChanneledNetwork.WILDCARD_CHANNEL) {
            return getPositions();
        }
        Set<PrioritizedPartPos> positions = this.positions.get(channel);
        Set<PrioritizedPartPos> wildcardPositions = this.positions.get(IChanneledNetwork.WILDCARD_CHANNEL);
        if (positions == null) positions = Collections.emptySet();
        if (wildcardPositions == null) wildcardPositions = Collections.emptySet();
        return ImmutableSet.copyOf(Iterables.concat(positions, wildcardPositions));
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
    public PositionsIterator getPositionIterator(int channel) {
        PositionsIterator it = positionsIterators.get(channel);
        if (it == null) {
            // If no custom iterator was given, iterate in first-come-first-serve order
            it = createPositionIterator(channel);
        } else {
            it = it.cloneState();
        }
        return it;
    }

    @Override
    public void setPositionIterator(@Nullable PositionsIterator iterator, int channel) {
        if (iterator == null || !iterator.hasNext()) {
            positionsIterators.remove(channel);
        } else {
            positionsIterators.put(channel, iterator);
        }
    }

    @Override
    public PositionsIterator createPositionIterator(int channel) {
        PositionsIterator it = new PositionsIterator(getPositions(channel), this);
        onPositionIteratorCreated(it);
        return it;
    }

    @Override
    public void onPositionIteratorCreated(PositionsIterator positionsIterator) {
        createdIterators.add(positionsIterator);
    }

    protected void invalidateIterators() {
        this.positionsIterators.clear();
        this.createdIterators.forEach(PositionsIterator::invalidate);
        this.createdIterators.clear();
    }

    @Override
    public boolean addPosition(PartPos pos, int priority, int channel) {
        invalidateIterators();

        Set<PrioritizedPartPos> positions = this.positions.get(channel);
        if (positions == null) {
            positions = Sets.newTreeSet();
            this.positions.put(channel, positions);
        }
        return positions.add(PrioritizedPartPos.of(pos, priority));
    }

    @Override
    public void removePosition(PartPos pos) {
        invalidateIterators();

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
