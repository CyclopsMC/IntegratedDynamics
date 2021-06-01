package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.cyclops.commoncapabilities.api.capability.inventorystate.IInventoryState;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.ingredient.collection.diff.IngredientCollectionDiff;
import org.cyclops.cyclopscore.ingredient.collection.diff.IngredientCollectionDiffManager;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentStorageObservable;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.PrioritizedPartPos;
import org.cyclops.integrateddynamics.core.network.diagnostics.NetworkDiagnostics;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Instances of this class are able to watch ingredient positions and emit diffs.
 *
 * @author rubensworks
 */
public class IngredientObserver<T, M> {

    private static final ExecutorService WORKER_POOL = Executors.newFixedThreadPool(GeneralConfig.ingredientNetworkObserverThreads);
    static {
        MinecraftForge.EVENT_BUS.addListener((Consumer<FMLServerStoppingEvent>) event -> {
            if (event.getServer().isDedicatedServer()) {
                WORKER_POOL.shutdown();
            }
        });
    }

    private final IPositionedAddonsNetworkIngredients<T, M> network;

    private final Set<IIngredientComponentStorageObservable.IIndexChangeObserver<T, M>> changeObservers;
    private final Int2ObjectMap<Map<PartPos, Integer>> observeTargetTickIntervals;
    private final Int2ObjectMap<Map<PartPos, Integer>> observeTargetTicks;
    private final Int2ObjectMap<Map<PrioritizedPartPos, IngredientCollectionDiffManager<T, M>>> channeledDiffManagers;
    private final Int2ObjectMap<Set<PartPos>> pendingTickResets;

    private final Int2ObjectMap<List<PrioritizedPartPos>> lastRemoved;
    private final Map<PartPos, Integer> lastInventoryStates;
    private Future<?> lastObserverBarrier;
    private boolean runningObserverSync;

    public IngredientObserver(IPositionedAddonsNetworkIngredients<T, M> network) {
        this.network = network;
        this.changeObservers = Sets.newIdentityHashSet();
        this.observeTargetTickIntervals = new Int2ObjectOpenHashMap<>();
        this.observeTargetTicks = new Int2ObjectOpenHashMap<>();
        this.channeledDiffManagers = new Int2ObjectOpenHashMap<>();
        this.pendingTickResets = new Int2ObjectOpenHashMap<>();
        this.lastRemoved = new Int2ObjectOpenHashMap<>();
        this.lastInventoryStates = Maps.newHashMap();

        this.lastObserverBarrier = null;
        this.runningObserverSync = false;
    }

    public IPositionedAddonsNetworkIngredients<T, M> getNetwork() {
        return network;
    }

    @Nullable
    public List<PrioritizedPartPos> getLastRemoved(int channel) {
        return lastRemoved.get(channel);
    }

    public void onPositionRemoved(int channel, PrioritizedPartPos pos) {
        List<PrioritizedPartPos> positions = this.lastRemoved.get(channel);
        if (positions == null) {
            positions = Lists.newLinkedList();
            this.lastRemoved.put(channel, positions);
        }
        positions.add(pos);
        this.lastInventoryStates.remove(pos.getPartPos());
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
        return ServerLifecycleHooks.getCurrentServer().getTickCounter();
    }

    protected void emitEvent(IIngredientComponentStorageObservable.StorageChangeEvent<T, M> event) {
        if (GeneralConfig.ingredientNetworkObserverEnableMultithreading) {
            // Make sure we are running on the main server thread to avoid concurrency exceptions
            ServerLifecycleHooks.getCurrentServer().deferTask(() -> {
                for (IIngredientComponentStorageObservable.IIndexChangeObserver<T, M> observer : getObserversCopy()) {
                    observer.onChange(event);
                }
            });
        } else {
            for (IIngredientComponentStorageObservable.IIndexChangeObserver<T, M> observer : getObserversCopy()) {
                observer.onChange(event);
            }
        }
    }

    protected synchronized List<IIngredientComponentStorageObservable.IIndexChangeObserver<T, M>> getObserversCopy() {
        return Lists.newArrayList(this.changeObservers);
    }

    protected int[] getChannels() {
        int[] networkChannels = getNetwork().getChannels();
        IntSet lastRemovedChannels = this.lastRemoved.keySet();
        if (lastRemovedChannels.size() == 0) {
            return networkChannels;
        }
        // We use a set that maintains insertion order,
        // because we MUST iterate over the channels that have removals first!
        IntSet uniqueChannels = new IntLinkedOpenHashSet();
        for (int lastRemovedChannel : lastRemovedChannels) {
            uniqueChannels.add(lastRemovedChannel);
        }
        for (int networkChannel : networkChannels) {
            uniqueChannels.add(networkChannel);
        }
        return uniqueChannels.toIntArray();
    }

    /**
     * @param forceSync If observation should happen synchronously.
     * @return If an observation job was successfully started if it was needed.
     */
    protected boolean observe(boolean forceSync) {
        if (!this.changeObservers.isEmpty()) {
            // If we forcefully observe sync, make sure that no async observers are still running
            if (forceSync && GeneralConfig.ingredientNetworkObserverEnableMultithreading
                    && this.lastObserverBarrier != null && !this.lastObserverBarrier.isDone()) {
                // This loop is necessary because observation tasks may require chunk lookups,
                // which are only allowed on the main thread, and are therefore deferred as task on the main thread.
                // This loop makes sure that these tasks are handled.
                // Without this loop, we would run into a deadlock where the main- and worker-thread would both halt.
                do {
                    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                    for (ServerWorld serverworld : server.getWorlds()) {
                        serverworld.getChunkProvider().driveOneTask();
                    }
                    Thread.yield();
                } while (!this.lastObserverBarrier.isDone());
            }

            if (GeneralConfig.ingredientNetworkObserverEnableMultithreading && !forceSync) {
                // If we still have an uncompleted job (sync or async) from the previous tick, don't start a new one yet!
                if ((this.lastObserverBarrier != null && !this.lastObserverBarrier.isDone()) || this.runningObserverSync) {
                    return false;
                }

                // Schedule the observation job
                this.lastObserverBarrier = WORKER_POOL.submit(() -> {
                    for (int channel : getChannels()) {
                        observe(channel);
                    }
                });
            } else {
                // If we have an uncompleted sync observer, don't start a new one yet!
                if (this.runningObserverSync) {
                    return false;
                }

                this.runningObserverSync = true;
                for (int channel : getChannels()) {
                    observe(channel);
                }
                this.runningObserverSync = false;
            }
        }
        return true;
    }

    protected synchronized Set<PrioritizedPartPos> getPositionsCopy(int channel) {
        return Sets.newHashSet(getNetwork().getPrioritizedPositions(channel));
    }

    protected void observe(int channel) {
        int currentTick = getCurrentTick();

        // Prepare ticking collections
        Map<PartPos, Integer> channelTargetTicks = observeTargetTicks.get(channel);
        if (channelTargetTicks == null) {
            channelTargetTicks = Maps.newHashMap();
        }
        Map<PartPos, Integer> channelIntervals = this.observeTargetTickIntervals.get(channel);
        if (channelIntervals == null) {
            channelIntervals = Maps.newHashMap();
        }

        // Calculate diff of all positions
        Map<PrioritizedPartPos, IngredientCollectionDiffManager<T, M>> diffManagers = this.channeledDiffManagers.get(channel);
        if (diffManagers == null) {
            diffManagers = Maps.newHashMap();
            this.channeledDiffManagers.put(channel, diffManagers);
        }

        // Check if we should diagnoze the observer
        boolean isBeingDiagnozed = NetworkDiagnostics.getInstance().isBeingDiagnozed();
        Map<PartPos, Long> lastSecondDurations = network.getLastSecondDurationIndex();
        if (!isBeingDiagnozed && !lastSecondDurations.isEmpty()) {
            // Make sure we aren't using any unnecessary memory.
            lastSecondDurations.clear();
        }

        // Emit diffs for all current positions
        Set<PrioritizedPartPos> positions = getPositionsCopy(channel);
        for (PrioritizedPartPos partPos : positions) {
            // Get current time if diagnostics are enabled
            long startTime = 0;
            if (isBeingDiagnozed) {
                startTime = System.nanoTime();
            }

            // Check if we should observe this position in this tick
            int lastTick = channelTargetTicks.getOrDefault(partPos.getPartPos(), currentTick);
            if (lastTick <= currentTick) {
                // Remove this position from the pending tick reset set
                synchronized (this.pendingTickResets) {
                    Set<PartPos> pendingTickResetsChannel = this.pendingTickResets.get(channel);
                    if (pendingTickResetsChannel != null) {
                        pendingTickResetsChannel.remove(partPos.getPartPos());
                        if (pendingTickResetsChannel.isEmpty()) {
                            this.pendingTickResets.remove(channel);
                        }
                    }
                }

                // If an inventory state is exposed, check if it has changed since the last observation call.
                boolean skipPosition = false;

                // Skip position forcefully if it is not loaded
                if (!partPos.getPartPos().getPos().isLoaded()) {
                    skipPosition = true;
                }

                if (!skipPosition) {
                    TileHelpers.UNSAFE_TILE_ENTITY_GETTER = true;
                    IInventoryState inventoryState = TileHelpers.getCapability(partPos.getPartPos().getPos(),
                            partPos.getPartPos().getSide(), Capabilities.INVENTORY_STATE).orElse(null);
                    TileHelpers.UNSAFE_TILE_ENTITY_GETTER = false;
                    if (inventoryState != null) {
                        Integer lastState = this.lastInventoryStates.get(partPos.getPartPos());
                        int newState = inventoryState.getState();
                        if (lastState != null && lastState == newState) {
                            // Skip this position if it hasn't not changed
                            skipPosition = true;
                        } else {
                            this.lastInventoryStates.put(partPos.getPartPos(), newState);
                        }
                    }
                }

                if (!skipPosition) {
                    IngredientCollectionDiffManager<T, M> diffManager = diffManagers.get(partPos);
                    if (diffManager == null) {
                        diffManager = new IngredientCollectionDiffManager<>(network.getComponent());
                        diffManagers.put(partPos, diffManager);
                    }

                    // Emit event of diff
                    Iterator<T> instances = getNetwork().getRawInstances(partPos.getPartPos());
                    IngredientCollectionDiff<T, M> diff = diffManager.onChange(instances);
                    boolean hasChanges = false;
                    if (diff.hasAdditions()) {
                        hasChanges = true;
                        this.emitEvent(new IIngredientComponentStorageObservable.StorageChangeEvent<>(channel, partPos,
                                IIngredientComponentStorageObservable.Change.ADDITION, false, diff.getAdditions()));
                    }
                    if (diff.hasDeletions()) {
                        hasChanges = true;
                        this.emitEvent(new IIngredientComponentStorageObservable.StorageChangeEvent<>(channel, partPos,
                                IIngredientComponentStorageObservable.Change.DELETION, diff.isCompletelyEmpty(), diff.getDeletions()));
                    }

                    // Update the next tick value
                    int tickInterval = channelIntervals.getOrDefault(partPos.getPartPos(), GeneralConfig.ingredientNetworkObserverFrequencyMax);
                    // Decrease the frequency when changes were detected
                    // Increase the frequency when no changes were detected
                    // This will make it so that quickly changing storages will be observed
                    // more frequently than slowly changing storages
                    boolean tickIntervalChanged = false;
                    if (hasChanges) {
                        if (tickInterval > GeneralConfig.ingredientNetworkObserverFrequencyMin) {
                            tickIntervalChanged = true;
                            tickInterval = Math.max(GeneralConfig.ingredientNetworkObserverFrequencyMin, tickInterval - GeneralConfig.ingredientNetworkObserverFrequencyDecreaseFactor);
                        }
                    } else {
                        if (tickInterval < GeneralConfig.ingredientNetworkObserverFrequencyMax) {
                            tickIntervalChanged = true;
                            tickInterval = Math.min(GeneralConfig.ingredientNetworkObserverFrequencyMax, tickInterval + GeneralConfig.ingredientNetworkObserverFrequencyIncreaseFactor);
                        }
                    }
                    // No need to store the interval if it == 1, as the previous or default value will
                    // definitely also cause this part to tick in next tick.
                    // This makes these cases slightly faster, as no map updates are needed.
                    if (tickInterval != 1) {
                        channelTargetTicks.put(partPos.getPartPos(), currentTick + tickInterval);

                    }
                    // Only update when the interval has changed.
                    // In most cases, this will remain the same.
                    if (tickIntervalChanged) {
                        if (tickInterval != GeneralConfig.ingredientNetworkObserverFrequencyMax) {
                            channelIntervals.put(partPos.getPartPos(), tickInterval);
                        } else {
                            channelIntervals.remove(partPos.getPartPos());
                        }
                    }
                }
            }

            // Calculate duration if diagnostics are enabled
            if (isBeingDiagnozed) {
                long duration = System.nanoTime() - startTime;
                PartPos interfacePos = PartTarget.fromCenter(partPos.getPartPos()).getTarget();
                Long lastDuration = lastSecondDurations.get(interfacePos);
                if (lastDuration != null) {
                    duration = duration + lastDuration;
                }
                lastSecondDurations.put(interfacePos, duration);
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
        if (!channelIntervals.isEmpty()) {
            observeTargetTickIntervals.put(channel, channelIntervals);
        }
    }

    public void resetTickInterval(int channel, PartPos targetPos) {
        // Reset the channel ticks
        Map<PartPos, Integer> channelTicks = this.observeTargetTicks.get(channel);
        if (channelTicks == null) {
            channelTicks = Maps.newHashMap();
            this.observeTargetTicks.put(channel, channelTicks);
        }
        channelTicks.put(targetPos, getCurrentTick() + GeneralConfig.ingredientNetworkObserverFrequencyForced);

        // Keep an overview of the pending positions per channel that require tick resets
        synchronized (this.pendingTickResets) {
            Set<PartPos> pendingTickResetsChannel = this.pendingTickResets.get(channel);
            if (pendingTickResetsChannel == null) {
                pendingTickResetsChannel = Sets.newHashSet();
                this.pendingTickResets.put(channel, pendingTickResetsChannel);
            }
            pendingTickResetsChannel.add(targetPos);
        }
    }

    public boolean isTickResetPending(int channel) {
        synchronized (this.pendingTickResets) {
            return this.pendingTickResets.containsKey(channel);
        }
    }

}
