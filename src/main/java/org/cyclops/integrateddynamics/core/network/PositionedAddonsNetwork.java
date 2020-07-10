package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
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
import java.util.Map;
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
    private final Int2ObjectMap<Set<PrioritizedPartPos>> positions = new Int2ObjectOpenHashMap<>();
    private final Map<PartPos, Integer> positionChannels = Maps.newHashMap();
    // We store the thread id together with the disabled position.
    // This is to make sure that different threads can safely iterate over positions in parallel
    // without clashing with each other, as this could lead to problems such as in #194.
    // This for example applies to the ingredient observer and in-world ingredient movement.
    private final Set<Pair<Long, PartPos>> disabledPositions = Sets.newHashSet();

    private IPartPosIteratorHandler partPosIteratorHandler = null;

    @Override
    public int[] getChannels() {
        return positions.keySet().toIntArray();
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

    @Override
    public int getPositionChannel(PartPos pos) {
        return this.positionChannels.getOrDefault(pos, -1);
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
            this.positionChannels.put(pos, channel);
            this.onPositionAdded(channel, prioritizedPosition);
            return true;
        }
        return false;
    }

    protected void onPositionAdded(int channel, PrioritizedPartPos pos) {

    }

    @Override
    public void removePosition(PartPos pos) {
        invalidateIterators();

        Wrapper<Integer> removedChannel = new Wrapper<>(-2);
        Wrapper<PrioritizedPartPos> removedPos = new Wrapper<>(null);
        for (Int2ObjectMap.Entry<Set<PrioritizedPartPos>> entry : this.positions.int2ObjectEntrySet()) {
            int channel = entry.getIntKey();
            Set<PrioritizedPartPos> positions = entry.getValue();
            Iterator<PrioritizedPartPos> it = positions.iterator();
            while (it.hasNext()) {
                PrioritizedPartPos prioritizedPartPos = it.next();
                if (prioritizedPartPos.getPartPos().equals(pos)) {
                    it.remove();
                    allPositions.remove(prioritizedPartPos);
                    removedPos.set(prioritizedPartPos);
                    removedChannel.set(channel);
                    break;
                }
            }
        }
        int channel = removedChannel.get();
        if (channel != -2) {
            this.onPositionRemoved(channel, removedPos.get());
            if (positions.get(channel).isEmpty()) {
                this.positions.remove(channel);
            }
        }
        positionChannels.remove(pos);
    }

    protected void onPositionRemoved(int channel, PrioritizedPartPos pos) {

    }

    @Override
    public boolean isPositionDisabled(PartPos pos) {
        return disabledPositions.contains(Pair.of(Thread.currentThread().getId(), pos));
    }

    @Override
    public void disablePosition(PartPos pos) {
        disabledPositions.add(Pair.of(Thread.currentThread().getId(), pos));
    }

    @Override
    public void enablePosition(PartPos pos) {
        disabledPositions.remove(Pair.of(Thread.currentThread().getId(), pos));
    }

}
