package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.Getter;
import lombok.Setter;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.api.part.PartPos;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

/**
 * A network that can hold prioritized positions.
 * @author rubensworks
 */
public abstract class PositionedAddonsNetwork implements IPositionedAddonsNetwork {

    @Getter
    @Setter
    private INetwork network;
    private final TIntObjectMap<Set<PrioritizedPartPos>> positions = new TIntObjectHashMap<>();
    private final Set<PartPos> disabledPositions = Sets.newHashSet();

    private final TIntObjectMap<PositionsIterator> positionsIterators = new TIntObjectHashMap<>();
    private final Set<PositionsIterator> createdIterators = Collections.newSetFromMap(new WeakHashMap<>());

    @Override
    public int[] getChannels() {
        return positions.keys();
    }

    @Override
    public boolean hasPositions() {
        return !positions.isEmpty();
    }

    @Override
    public Collection<PartPos> getPositions(int channel) {
        if (channel == WILDCARD_CHANNEL) {
            return getPositions();
        }
        Set<PrioritizedPartPos> positions = this.positions.get(channel);
        Set<PrioritizedPartPos> wildcardPositions = this.positions.get(WILDCARD_CHANNEL);
        if (positions == null) positions = Collections.emptySet();
        if (wildcardPositions == null) wildcardPositions = Collections.emptySet();
        TreeSet<PrioritizedPartPos> merged = Sets.newTreeSet();
        merged.addAll(positions);
        merged.addAll(wildcardPositions);
        return merged.stream().map(PrioritizedPartPos::getPartPos).collect(Collectors.toList());
    }

    @Override
    public Collection<PartPos> getPositions() {
        List<PartPos> allPositions = Lists.newArrayList();
        for (Set<PrioritizedPartPos> positions : this.positions.valueCollection()) {
            for (PrioritizedPartPos position : positions) {
                allPositions.add(position.getPartPos());
            }
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

        Wrapper<Integer> removedChannel = new Wrapper<>(-2);
        this.positions.forEachEntry((channel, positions) -> {
            if (positions.removeIf(prioritizedPartPos -> prioritizedPartPos.getPartPos().equals(pos))) {
                removedChannel.set(channel);
                return false;
            }
            return true;
        });
        int channel = removedChannel.get();
        if (channel != -2) {
            this.onPositionRemoved(channel, pos);
            if (positions.get(channel).isEmpty()) {
                this.positions.remove(channel);
            }
        }
    }

    protected void onPositionRemoved(int channel, PartPos pos) {

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
