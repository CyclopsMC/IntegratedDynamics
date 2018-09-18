package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.cyclops.cyclopscore.ingredient.collection.diff.IngredientCollectionDiff;
import org.cyclops.cyclopscore.ingredient.collection.diff.IngredientCollectionDiffManager;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentStorageObservable;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.api.part.PrioritizedPartPos;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Instances of this class are able to watch ingredient positions and emit diffs.
 *
 * @author rubensworks
 */
public class IngredientObserver<T, M> {

    private final IPositionedAddonsNetworkIngredients<T, M> network;

    private final Set<IIngredientComponentStorageObservable.IIndexChangeObserver<T, M>> changeObservers;
    private final TIntObjectMap<Map<PrioritizedPartPos, Integer>> observeTargetTickIntervals;
    private final TIntObjectMap<Map<PrioritizedPartPos, Integer>> observeTargetTicks;
    private final TIntObjectMap<Map<PrioritizedPartPos, IngredientCollectionDiffManager<T, M>>> channeledDiffManagers;

    private final TIntObjectMap<List<PrioritizedPartPos>> lastRemoved;

    public IngredientObserver(IPositionedAddonsNetworkIngredients<T, M> network) {
        this.network = network;
        this.changeObservers = Sets.newIdentityHashSet();
        this.observeTargetTickIntervals = new TIntObjectHashMap<>();
        this.observeTargetTicks = new TIntObjectHashMap<>();
        this.channeledDiffManagers = new TIntObjectHashMap<>();
        this.lastRemoved = new TIntObjectHashMap<>();
    }

    public IPositionedAddonsNetworkIngredients<T, M> getNetwork() {
        return network;
    }

    public void onPositionRemoved(int channel, PrioritizedPartPos pos) {
        List<PrioritizedPartPos> positions = this.lastRemoved.get(channel);
        if (positions == null) {
            positions = Lists.newLinkedList();
            this.lastRemoved.put(channel, positions);
        }
        positions.add(pos);
    }

    /**
     * Remove the observer tick interval for the given position.
     * This will virtually set it to the default value.
     * @param partPos The position.
     * @param channel The channel of the position.
     */
    public void removePositionObserverTickInterval(PrioritizedPartPos partPos, int channel) {
        Map<PrioritizedPartPos, Integer> channelIntervals = this.observeTargetTickIntervals.get(channel);
        if (channelIntervals != null) {
            channelIntervals.remove(partPos);
            if (channelIntervals.isEmpty()) {
                this.observeTargetTickIntervals.remove(channel);
            }
        }
    }

    /**
     * Set the observer tick interval for the given position.
     * @param partPos The position.
     * @param channel The channel of the position.
     * @param interval The tick interval.
     */
    public void setPositionObserverTickInterval(PrioritizedPartPos partPos, int channel, int interval) {
        if (interval <= 1) {
            removePositionObserverTickInterval(partPos, channel);
        } else {
            Map<PrioritizedPartPos, Integer> channelIntervals = this.observeTargetTickIntervals.get(channel);
            if (channelIntervals == null) {
                channelIntervals = Maps.newHashMap();
                this.observeTargetTickIntervals.put(channel, channelIntervals);
            }
            channelIntervals.put(partPos, interval);
        }
    }

    /**
     * Add an observer for listing to index change events.
     * @param observer An index change observer.
     */
    public synchronized void addChangeObserver(IIngredientComponentStorageObservable.IIndexChangeObserver<T, M> observer) {
        changeObservers.add(observer);
    }

    /**
     * Remove the given index change observer.
     * This will silently fail if the given observer was not registered.
     * @param observer An index change observer.
     */
    public synchronized void removeChangeObserver(IIngredientComponentStorageObservable.IIndexChangeObserver<T, M> observer) {
        changeObservers.remove(observer);
    }

    protected int getCurrentTick() {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
    }

    protected void emitEvent(IIngredientComponentStorageObservable.StorageChangeEvent<T, M> event) {
        for (IIngredientComponentStorageObservable.IIndexChangeObserver<T, M> observer : getObserversCopy()) {
            observer.onChange(event);
        }
    }

    protected synchronized List<IIngredientComponentStorageObservable.IIndexChangeObserver<T, M>> getObserversCopy() {
        return Lists.newArrayList(this.changeObservers);
    }

    protected void observe() {
        List<IIngredientComponentStorageObservable.IIndexChangeObserver<T, M>> observers = this.getObserversCopy();
        if (observers != null) {
            for (int channel : getNetwork().getChannels()) {
                observe(channel);
            }
        }
    }

    protected synchronized Set<PrioritizedPartPos> getPositionsCopy(int channel) {
        return Sets.newHashSet(getNetwork().getPrioritizedPositions(channel));
    }

    protected void observe(int channel) {
        int currentTick = getCurrentTick();

        // Prepare ticking collections
        Map<PrioritizedPartPos, Integer> channelTargetTicks = observeTargetTicks.get(channel);
        if (channelTargetTicks == null) {
            channelTargetTicks = Maps.newHashMap();
        }
        Map<PrioritizedPartPos, Integer> channelIntervals = this.observeTargetTickIntervals.get(channel);

        // Calculate diff of all positions
        Map<PrioritizedPartPos, IngredientCollectionDiffManager<T, M>> diffManagers = this.channeledDiffManagers.get(channel);
        if (diffManagers == null) {
            diffManagers = Maps.newHashMap();
            this.channeledDiffManagers.put(channel, diffManagers);
        }

        // Emit diffs for all current positions
        Set<PrioritizedPartPos> positions = getPositionsCopy(channel);
        for (PrioritizedPartPos partPos : positions) {
            // Check if we should observe this position in this tick
            int lastTick = channelTargetTicks.getOrDefault(partPos, currentTick);
            if (lastTick <= currentTick) {
                IngredientCollectionDiffManager<T, M> diffManager = diffManagers.get(partPos);
                if (diffManager == null) {
                    diffManager = new IngredientCollectionDiffManager<>(network.getComponent());
                    diffManagers.put(partPos, diffManager);
                }

                // Emit event of diff
                IngredientCollectionDiff<T, M> diff = diffManager.onChange(getNetwork().getRawInstances(partPos.getPartPos()));
                if (diff.hasAdditions()) {
                    this.emitEvent(new IIngredientComponentStorageObservable.StorageChangeEvent<>(channel, partPos,
                            IIngredientComponentStorageObservable.Change.ADDITION, false, diff.getAdditions()));
                }
                if (diff.hasDeletions()) {
                    this.emitEvent(new IIngredientComponentStorageObservable.StorageChangeEvent<>(channel, partPos,
                            IIngredientComponentStorageObservable.Change.DELETION, diff.isCompletelyEmpty(), diff.getDeletions()));
                }

                // Update the next tick value
                int tickInterval = GeneralConfig.defaultIngredientNetworkObserverFrequency;
                if (channelIntervals != null) {
                    tickInterval = channelIntervals.getOrDefault(partPos, tickInterval);
                }
                if (tickInterval != 1) {
                    // No need to store the value, as the previous or default value will
                    // definitely also cause this part to tick in next tick.
                    // This makes these cases slightly faster, as no map updates are needed.
                    channelTargetTicks.put(partPos, lastTick + tickInterval);
                }
            }
        }

        // Emit deletions for all removed positions
        List<PrioritizedPartPos> lastRemovedPositions = this.lastRemoved.get(channel);
        if (lastRemovedPositions != null) {
            for (PrioritizedPartPos partPos : lastRemovedPositions) {
                IngredientCollectionDiffManager<T, M> diffManager = diffManagers.get(partPos);
                if (diffManager != null) {
                    // Emit event of diff with *empty* iterator
                    IngredientCollectionDiff<T, M> diff = diffManager.onChange(Iterators.forArray());
                    // No additions are possible
                    if (diff.hasDeletions()) {
                        this.emitEvent(new IIngredientComponentStorageObservable.StorageChangeEvent<>(channel, partPos,
                                IIngredientComponentStorageObservable.Change.DELETION, diff.isCompletelyEmpty(), diff.getDeletions()));
                    }
                }
            }
            this.lastRemoved.remove(channel);
        }

        // Store our new ticking collections
        if (!channelTargetTicks.isEmpty()) {
            observeTargetTicks.put(channel, channelTargetTicks);
        }
    }

}
