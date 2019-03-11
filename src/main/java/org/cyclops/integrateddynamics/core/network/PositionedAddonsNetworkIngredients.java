package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.util.EnumFacing;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.cyclopscore.ingredient.collection.IIngredientCollection;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentStorageObservable;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientPositionsIndex;
import org.cyclops.integrateddynamics.api.network.IFullNetworkListener;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PrioritizedPartPos;
import org.cyclops.integrateddynamics.api.path.IPathElement;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * An ingredient network that can hold prioritized positions.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter, may be Void. Instances MUST properly implement the equals method.
 * @author rubensworks
 */
public abstract class PositionedAddonsNetworkIngredients<T, M> extends PositionedAddonsNetwork
        implements IPositionedAddonsNetworkIngredients<T, M>, IFullNetworkListener,
        IIngredientComponentStorageObservable.IIndexChangeObserver<T, M> {

    private final IngredientComponent<T, M> component;

    private final IngredientObserver<T, M> ingredientObserver;
    private final Int2ObjectMap<IngredientPositionsIndex<T, M>> indexes;

    private boolean observe;
    private Map<PartPos, Long> lastSecondDurations = Maps.newHashMap();

    public PositionedAddonsNetworkIngredients(IngredientComponent<T, M> component) {
        this.component = component;

        this.ingredientObserver = new IngredientObserver<>(this);
        this.ingredientObserver.addChangeObserver(this);
        this.indexes = new Int2ObjectOpenHashMap<>();

        this.observe = false;
    }

    @Override
    public IngredientComponent<T, M> getComponent() {
        return component;
    }

    @Nullable
    public IIngredientPositionsIndex<T, M> getInstanceLocationsIndex(int channel) {
        return this.indexes.get(channel);
    }

    @Override
    public boolean addPosition(PartPos pos, int priority, int channel) {
        return getPositionedStorageUnsafe(pos) != null && super.addPosition(pos, priority, channel);
    }

    @Override
    public void onChange(IIngredientComponentStorageObservable.StorageChangeEvent<T, M> event) {
        applyChangesToChannel(event, event.getChannel());
        applyChangesToChannel(event, -1); // Apply all changes to "all" channels

        if (GeneralConfig.logChangeEvents) {
            System.out.println(this.toString() + event);
        }
    }

    protected void applyChangesToChannel(IIngredientComponentStorageObservable.StorageChangeEvent<T, M> event, int channel) {
        IIngredientCollection<T, M> instances = event.getInstances();
        PrioritizedPartPos pos = event.getPos();
        IngredientPositionsIndex<T, M> index = getIndexSafe(channel);
        if (event.getChangeType() == IIngredientComponentStorageObservable.Change.DELETION) {
            index.removeAll(instances);
            if (event.isCompleteChange()) {
                for (T instance : instances) {
                    index.removePosition(instance, pos);
                }
            }

            // Cleanup empty collections
            if (index.isEmpty()) {
                this.indexes.remove(channel);
            }
        } else if (event.getChangeType() == IIngredientComponentStorageObservable.Change.ADDITION) {
            index.addAll(instances);
            for (T instance : instances) {
                index.addPosition(instance, pos);
            }
        }
    }

    protected IngredientPositionsIndex<T, M> getIndexSafe(int channel) {
        IngredientPositionsIndex<T, M> index = this.indexes.get(channel);
        if (index == null) {
            index = new IngredientPositionsIndex<>(getComponent());
            this.indexes.put(channel, index);
        }
        return index;
    }

    @Override
    protected void onPositionAdded(int channel, PrioritizedPartPos pos) {
        super.onPositionAdded(channel, pos);

        // If our position was added to the lastRemoved list without it being processed yet,
        // remove it from the list before that processing is going to start.
        List<PrioritizedPartPos> lastRemoved = ingredientObserver.getLastRemoved(channel);
        if (lastRemoved != null) {
            lastRemoved.remove(pos);
        }
    }

    @Override
    protected void onPositionRemoved(int channel, PrioritizedPartPos pos) {
        super.onPositionRemoved(channel, pos);
        ingredientObserver.onPositionRemoved(channel, pos);
    }

    @Override
    public IIngredientComponentStorage<T, M> getChannel(int channel) {
        return new IngredientChannelIndexed<>(this, channel, getChannelIndex(channel));
    }

    @Override
    public void addObserver(IIndexChangeObserver<T, M> observer) {
        this.ingredientObserver.addChangeObserver(observer);
    }

    @Override
    public void removeObserver(IIndexChangeObserver<T, M> observer) {
        this.ingredientObserver.removeChangeObserver(observer);
    }

    @Override
    public void scheduleObservation() {
        this.observe = true;
    }

    @Override
    public void scheduleObservationForced(int channel, PartPos pos) {
        scheduleObservation();
        if (channel == IPositionedAddonsNetwork.WILDCARD_CHANNEL) {
            this.ingredientObserver.resetTickInterval(getPositionChannel(pos), pos);
        } else {
            this.ingredientObserver.resetTickInterval(channel, pos);
        }
    }

    @Override
    public boolean shouldObserve() {
        return this.observe;
    }

    @Override
    public IIngredientPositionsIndex<T, M> getChannelIndex(int channel) {
        IIngredientPositionsIndex<T, M> index = getInstanceLocationsIndex(channel);
        if (index == null) {
            // This can occur when the index is empty,
            // which can be caused by all attached storages being empty or no storages being available.
            index = new IngredientPositionsIndexEmpty<>(getComponent());
        }
        return index;
    }

    @Override
    public boolean addNetworkElement(INetworkElement element, boolean networkPreinit) {
        return true;
    }

    @Override
    public boolean removeNetworkElementPre(INetworkElement element) {
        return true;
    }

    @Override
    public void removeNetworkElementPost(INetworkElement element) {

    }

    @Override
    public void kill() {

    }

    public void update() {
        if (this.shouldObserve()) {
            this.ingredientObserver.observe();
            this.observe = false;
        }
    }

    @Override
    public boolean removePathElement(IPathElement pathElement, EnumFacing side) {
        return true;
    }

    @Override
    public void afterServerLoad() {

    }

    @Override
    public void beforeServerStop() {

    }

    @Override
    public boolean canUpdate(INetworkElement element) {
        return true;
    }

    @Override
    public void onSkipUpdate(INetworkElement element) {

    }

    @Override
    public void postUpdate(INetworkElement element) {

    }

    @Override
    public Map<PartPos, Long> getLastSecondDurationIndex() {
        return lastSecondDurations;
    }

    @Override
    public void resetLastSecondDurationsIndex() {
        lastSecondDurations.clear();
    }

    @Override
    public void invalidateElement(INetworkElement element) {

    }

    @Override
    public void revalidateElement(INetworkElement element) {

    }
}
