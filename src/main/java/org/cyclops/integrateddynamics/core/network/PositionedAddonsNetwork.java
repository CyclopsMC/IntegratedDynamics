package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.Sets;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.Getter;
import lombok.Setter;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartPosIteratorHandler;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PrioritizedPartPos;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * A network that can hold prioritized positions.
 * @author rubensworks
 */
public abstract class PositionedAddonsNetwork implements IPositionedAddonsNetwork {

    @Getter
    @Setter
    private INetwork network;
    private final Set<PrioritizedPartPos> allPositions = Sets.newTreeSet();
    private final TIntObjectMap<Set<PrioritizedPartPos>> positions = new TIntObjectHashMap<>();
    private final Set<PartPos> disabledPositions = Sets.newHashSet();

    private IPartPosIteratorHandler partPosIteratorHandler = null;

    @Override
    public int[] getChannels() {
        return positions.keys();
    }

    @Override
    public boolean hasPositions() {
        return !positions.isEmpty();
    }

    @Override
    public Collection<PrioritizedPartPos> getPrioritizedPositions(int channel) {
        if (channel == WILDCARD_CHANNEL) {
            return getPrioritizedPositions();
        }
        Set<PrioritizedPartPos> positions = this.positions.get(channel);
        Set<PrioritizedPartPos> wildcardPositions = this.positions.get(WILDCARD_CHANNEL);
        if (positions == null) {
            if (wildcardPositions != null) {
                return wildcardPositions;
            }
            positions = Collections.emptySet();
        }
        if (wildcardPositions == null) {
            return positions;
        }
        TreeSet<PrioritizedPartPos> merged = Sets.newTreeSet();
        merged.addAll(positions);
        merged.addAll(wildcardPositions);
        return merged;
    }

    @Override
    public Collection<PrioritizedPartPos> getPrioritizedPositions() {
        return this.allPositions;
    }

    protected void invalidateIterators() {
        setPartPosIteratorHandler(null);
    }

    @Override
    public void setPartPosIteratorHandler(@Nullable IPartPosIteratorHandler iteratorHandler) {
        this.partPosIteratorHandler = iteratorHandler;
    }

    @Nullable
    @Override
    public IPartPosIteratorHandler getPartPosIteratorHandler() {
        if (partPosIteratorHandler != null) {
            return partPosIteratorHandler;
        }
        return null;
    }

    @Override
    public boolean addPosition(PartPos pos, int priority, int channel) {
        invalidateIterators();

        PrioritizedPartPos prioritizedPosition = PrioritizedPartPos.of(pos, priority);
        if (allPositions.add(prioritizedPosition)) {
            Set<PrioritizedPartPos> positions = this.positions.get(channel);
            if (positions == null) {
                positions = Sets.newTreeSet();
                this.positions.put(channel, positions);
            }
            positions.add(prioritizedPosition);
            return true;
        }
        return false;
    }

    @Override
    public void removePosition(PartPos pos) {
        invalidateIterators();

        Wrapper<Integer> removedChannel = new Wrapper<>(-2);
        Wrapper<PrioritizedPartPos> removedPos = new Wrapper<>(null);
        this.positions.forEachEntry((channel, positions) -> {
            Iterator<PrioritizedPartPos> it = positions.iterator();
            while (it.hasNext()) {
                PrioritizedPartPos prioritizedPartPos = it.next();
                if (prioritizedPartPos.getPartPos().equals(pos)) {
                    it.remove();
                    allPositions.remove(prioritizedPartPos);
                    removedPos.set(prioritizedPartPos);
                    removedChannel.set(channel);
                    return false;
                }
            }
            return true;
        });
        int channel = removedChannel.get();
        if (channel != -2) {
            this.onPositionRemoved(channel, removedPos.get());
            if (positions.get(channel).isEmpty()) {
                this.positions.remove(channel);
            }
        }
    }

    protected void onPositionRemoved(int channel, PrioritizedPartPos pos) {

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
