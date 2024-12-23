package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2IntAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.util.LazyOptional;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.PartStateException;
import org.cyclops.integrateddynamics.api.network.AttachCapabilitiesEventNetwork;
import org.cyclops.integrateddynamics.api.network.IEventListenableNetworkElement;
import org.cyclops.integrateddynamics.api.network.IFullNetworkListener;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.api.network.event.INetworkEventBus;
import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.api.path.ISidedPathElement;
import org.cyclops.integrateddynamics.capability.network.NetworkCarrierConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.path.SidedPathElement;
import org.cyclops.integrateddynamics.core.network.diagnostics.NetworkDiagnostics;
import org.cyclops.integrateddynamics.core.network.event.NetworkElementAddEvent;
import org.cyclops.integrateddynamics.core.network.event.NetworkElementRemoveEvent;
import org.cyclops.integrateddynamics.core.network.event.NetworkEventBus;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.path.Cluster;
import org.cyclops.integrateddynamics.core.path.PathFinder;
import org.cyclops.integrateddynamics.core.persist.world.NetworkWorldStorage;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A network instance that can hold a set of {@link INetworkElement}s.
 * Note that this network only contains references to the relevant data, it does not contain the actual information.
 * @author rubensworks
 */
public class Network implements INetwork {

    private Cluster baseCluster;

    private final INetworkEventBus eventBus = new NetworkEventBus();
    private final TreeSet<INetworkElement> elements = Sets.newTreeSet();
    private Object2IntMap<INetworkElement> updateableElementsTicks = null;
    private TreeSet<INetworkElement> invalidatedElements = Sets.newTreeSet();
    private Map<INetworkElement, Long> lastSecondDurations = Maps.newHashMap();

    private final CapabilityDispatcher capabilityDispatcher;
    private IFullNetworkListener[] fullNetworkListeners;

    private CompoundTag toRead = null;
    private volatile boolean changed = false;
    private volatile boolean killed = false;

    private boolean crashed = false;

    /**
     * Initiate a full network from the given start position.
     * @param sidedPathElement The sided path element to start from.
     * @return The newly formed network.
     */
    public static Network initiateNetworkSetup(ISidedPathElement sidedPathElement) {
        Network network = new Network(PathFinder.getConnectedCluster(sidedPathElement));
        NetworkWorldStorage.getInstance(IntegratedDynamics._instance).addNewNetwork(network);
        return network;
    }

    /**
     * Check if two networks are equal.
     * @param networkA A network.
     * @param networkB Another network.
     * @return If they are equal.
     */
    public static boolean areNetworksEqual(Network networkA, Network networkB) {
        return networkA.elements.containsAll(networkB.elements) && networkA.elements.size() == networkB.elements.size();
    }

    /**
     * This constructor should not be called, except for the process of constructing networks from NBT.
     */
    public Network() {
        this.baseCluster = new Cluster();
        this.capabilityDispatcher = gatherCapabilities();
        onConstruct();
    }

    /**
     * Create a new network from a given cluster of path elements.
     * Each path element will be checked if it has a {@link INetworkElementProvider} capability at its position
     * and will add all its elements to the network in that case.
     * Each path element that has an {@link org.cyclops.integrateddynamics.api.part.IPartContainer} capability
     * will have the network stored in its part container.
     * @param pathElements The path elements that make up the connections in the network which can potentially provide network
     *               elements.
     */
    public Network(Cluster pathElements) {
        this.baseCluster = pathElements;
        this.capabilityDispatcher = gatherCapabilities();
        onConstruct();
        deriveNetworkElements(baseCluster);
    }

    protected CapabilityDispatcher gatherCapabilities() {
        AttachCapabilitiesEventNetwork event = new AttachCapabilitiesEventNetwork(this);
        MinecraftForge.EVENT_BUS.post(event);
        List<IFullNetworkListener> listeners = event.getFullNetworkListeners();
        this.fullNetworkListeners = listeners.toArray(new IFullNetworkListener[listeners.size()]);
        return event.getCapabilities().size() > 0 ? new CapabilityDispatcher(event.getCapabilities(), event.getListeners()) : null;
    }

    protected IFullNetworkListener[] gatherFullNetworkListeners() {
        List<IFullNetworkListener> listeners = Lists.newArrayList();

        return listeners.toArray(new IFullNetworkListener[listeners.size()]);
    }

    protected void onConstruct() {

    }

    private void deriveNetworkElements(Cluster pathElements) {
        if(!killIfEmpty()) {
            for (ISidedPathElement sidedPathElement : pathElements) {
                Level world = sidedPathElement.getPathElement().getPosition().getLevel(true);
                BlockPos pos = sidedPathElement.getPathElement().getPosition().getBlockPos();
                Direction side = sidedPathElement.getSide();
                BlockEntityHelpers.getCapability(world, pos, side, NetworkCarrierConfig.CAPABILITY).ifPresent(networkCarrier -> {
                    // Correctly remove any previously saved network in this carrier
                    // and set the new network to this.
                    INetwork network = networkCarrier.getNetwork();
                    if (network != null) {
                        network.removePathElement(sidedPathElement.getPathElement(), side);
                    }
                    networkCarrier.setNetwork(null);
                    networkCarrier.setNetwork(this);
                });
                BlockEntityHelpers.getCapability(world, pos, side, NetworkElementProviderConfig.CAPABILITY).ifPresent(networkElementProvider -> {
                    for(INetworkElement element : networkElementProvider.createNetworkElements(world, pos)) {
                        addNetworkElement(element, true);
                    }
                });
            }
            onNetworkChanged();
        }
    }

    @Override
    public boolean isInitialized() {
        return updateableElementsTicks != null;
    }

    @Override
    public INetworkEventBus getEventBus() {
        return this.eventBus;
    }

    /**
     * Initialize the network element data.
     */
    public void initialize() {
        initialize(false);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof Network && areNetworksEqual(this, (Network) object);
    }

    @Override
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("baseCluster", this.baseCluster.toNBT());
        tag.putBoolean("crashed", this.crashed);
        if (this.capabilityDispatcher != null) {
            tag.put("ForgeCaps", this.capabilityDispatcher.serializeNBT());
        }
        return tag;
    }

    @Override
    public void fromNBT(CompoundTag tag) {
        // NBT reading is postponed until the first network tick, to ensure that the game is properly initialized.
        // Because other mods may register things such as dimensions at the same time when networks
        // are being constructed (as was the case in #349)
        this.toRead = tag;
    }

    public void fromNBTEffective(CompoundTag tag) {
        this.baseCluster.fromNBT(tag.getCompound("baseCluster"));
        this.crashed = tag.getBoolean("crashed");
        if (this.capabilityDispatcher != null && tag.contains("ForgeCaps")) {
            this.capabilityDispatcher.deserializeNBT(tag.getCompound("ForgeCaps"));
        }
        deriveNetworkElements(baseCluster);
        initialize(true);
    }

    @Override
    public synchronized boolean addNetworkElement(INetworkElement element, boolean networkPreinit) {
        for (IFullNetworkListener fullNetworkListener : this.fullNetworkListeners) {
            if (!fullNetworkListener.addNetworkElement(element, networkPreinit)) {
                return false;
            }
        }

        if(getEventBus().postCancelable(new NetworkElementAddEvent.Pre(this, element))) {
            elements.add(element);
            if (!element.onNetworkAddition(this)) {
                elements.remove(element);
                return false;
            }
            if (!networkPreinit) {
                addNetworkElementUpdateable(element);
            }
            if (element instanceof IEventListenableNetworkElement) {
                IEventListenableNetworkElement<?> listenableElement = (IEventListenableNetworkElement<?>) element;
                listenableElement.getNetworkEventListener().ifPresent(listener -> {
                    if (listener.hasEventSubscriptions()) {
                        for (Class<? extends INetworkEvent> eventType : listener.getSubscribedEvents()) {
                            getEventBus().register(listenableElement, eventType);
                        }
                    }
                });
            }
            getEventBus().post(new NetworkElementAddEvent.Post(this, element));
            onNetworkChanged();
            return true;
        }
        return false;
    }

    @Override
    public void addNetworkElementUpdateable(INetworkElement element) {
        if(element.isUpdate()) {
            updateableElementsTicks.put(element, 0);
        }
    }

    @Override
    public boolean removeNetworkElementPre(INetworkElement element) {
        for (IFullNetworkListener fullNetworkListener : this.fullNetworkListeners) {
            if (!fullNetworkListener.removeNetworkElementPre(element)) {
                return false;
            }
        }
        return getEventBus().postCancelable(new NetworkElementRemoveEvent.Pre(this, element));
    }

    @Override
    public synchronized void setPriorityAndChannel(INetworkElement element, int priority, int channel) {
        elements.remove(element);
        int oldTickValue = updateableElementsTicks.defaultReturnValue();
        if (element.isUpdate()) {
            oldTickValue = updateableElementsTicks.removeInt(element);
        }

        //noinspection deprecation
        element.setPriorityAndChannel(this, priority, channel);

        elements.add(element);
        if (element.isUpdate()) {
            updateableElementsTicks.put(
                element,
                oldTickValue == updateableElementsTicks.defaultReturnValue()
                    ? element.getUpdateInterval()
                    : oldTickValue
            );
        }
    }

    @Override
    public void removeNetworkElementPost(INetworkElement element) {
        for (IFullNetworkListener fullNetworkListener : this.fullNetworkListeners) {
            fullNetworkListener.removeNetworkElementPost(element);
        }
        if (element instanceof IEventListenableNetworkElement) {
            IEventListenableNetworkElement<?> listenableElement = (IEventListenableNetworkElement<?>) element;
            listenableElement.getNetworkEventListener().ifPresent(listener -> {
                if (listener.hasEventSubscriptions()) {
                    getEventBus().unregister(listenableElement);
                }
            });
        }
        element.beforeNetworkKill(this);
        element.onNetworkRemoval(this);
        elements.remove(element);
        removeNetworkElementUpdateable(element);
        invalidatedElements.remove(element); // The element may be invalidated (like in an unloaded chunk) when it is being removed.
        getEventBus().post(new NetworkElementRemoveEvent.Post(this, element));
        onNetworkChanged();
    }

    @Override
    public synchronized void removeNetworkElementUpdateable(INetworkElement element) {
        if (isInitialized()) {
            updateableElementsTicks.removeInt(element);
        }
    }

    /**
     * Called when a network is server-loaded or newly created.
     * @param silent If the element should not be notified for the network becoming alive.
     */
    protected void initialize(boolean silent) {
        updateableElementsTicks = new Object2IntAVLTreeMap<>();
        updateableElementsTicks.defaultReturnValue(Integer.MIN_VALUE);
        for(INetworkElement element : elements) {
            addNetworkElementUpdateable(element);
            if(!silent) {
                element.afterNetworkAlive(this);
            }
            element.afterNetworkReAlive(this);
        }

        // Once all elements are alive, send a single variable contents updated event.
        this.getEventBus().post(new VariableContentsUpdatedEvent(this));
    }

    @Override
    public void kill() {
        for (IFullNetworkListener fullNetworkListener : this.fullNetworkListeners) {
            fullNetworkListener.kill();
        }
        for(INetworkElement element : elements) {
            element.beforeNetworkKill(this);
        }
        killed = true;
    }

    @Override
    public boolean killIfEmpty() {
        if(baseCluster.isEmpty()) {
            kill();
            onNetworkChanged();
            return true;
        }
        return false;
    }

    @Override
    public boolean canUpdate(INetworkElement element) {
        for (IFullNetworkListener fullNetworkListener : this.fullNetworkListeners) {
            if (!fullNetworkListener.canUpdate(element)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void postUpdate(INetworkElement element) {
        for (IFullNetworkListener fullNetworkListener : this.fullNetworkListeners) {
            fullNetworkListener.postUpdate(element);
        }
    }

    @Override
    public void onSkipUpdate(INetworkElement element) {
        for (IFullNetworkListener fullNetworkListener : this.fullNetworkListeners) {
            fullNetworkListener.onSkipUpdate(element);
        }
    }

    @Override
    public void updateGuaranteed() {
        if (this.toRead != null) {
            this.fromNBTEffective(this.toRead);
            this.toRead = null;
        }
    }

    @Override
    public final synchronized void update() {
        this.changed = false;
        if(killIfEmpty() || killed) {
            NetworkWorldStorage.getInstance(IntegratedDynamics._instance).removeInvalidatedNetwork(this);
        } else {
            onUpdate();

            // Update updateable network elements
            boolean isBeingDiagnozed = NetworkDiagnostics.getInstance().isBeingDiagnozed();
            if (!isBeingDiagnozed && !lastSecondDurations.isEmpty()) {
                // Make sure we aren't using any unnecessary memory.
                lastSecondDurations.clear();
            }
            for (Object2IntMap.Entry<INetworkElement> entry : updateableElementsTicks.object2IntEntrySet()) {
                var element = entry.getKey();
                try {
                    if (isValid(element)) {
                        long startTime = 0;
                        if (isBeingDiagnozed) {
                            startTime = System.nanoTime();
                        }
                        int lastElementTick = entry.getIntValue();
                        if (canUpdate(element)) {
                            if (lastElementTick <= 0) {
                                entry.setValue(element.getUpdateInterval() - 1);
                                element.update(this);
                                postUpdate(element);
                            } else {
                                entry.setValue(lastElementTick - 1);
                            }
                        } else {
                            onSkipUpdate(element);
                            entry.setValue(lastElementTick - 1);
                        }
                        if (isBeingDiagnozed) {
                            long duration = System.nanoTime() - startTime;
                            Long lastDuration = lastSecondDurations.get(element);
                            if (lastDuration != null) {
                                duration = duration + lastDuration;
                            }
                            lastSecondDurations.put(element, duration);
                        }
                    }
                } catch (PartStateException e) {
                    IntegratedDynamics.clog(org.apache.logging.log4j.Level.WARN, "Attempted to tick a part that was not properly unloaded. " +
                            "Report this to the Integrated Dynamics issue tracker with details on what you did " +
                            "leading up to this stacktrace. The part was forcefully unloaded");
                    e.printStackTrace();
                    element.invalidate(this);
                }
            }
        }
    }

    protected void onUpdate() {
        for (IFullNetworkListener fullNetworkListener : this.fullNetworkListeners) {
            fullNetworkListener.update();
        }
    }

    @Override
    public synchronized boolean removePathElement(IPathElement pathElement, Direction side) {
        for (IFullNetworkListener fullNetworkListener : this.fullNetworkListeners) {
            if (!fullNetworkListener.removePathElement(pathElement, side)) {
                return false;
            }
        }
        if(baseCluster.remove(SidedPathElement.of(pathElement, null))) {
            DimPos position = pathElement.getPosition();
            INetworkElementProvider networkElementProvider = BlockEntityHelpers.getCapability(
                    position, side, NetworkElementProviderConfig.CAPABILITY).orElse(null);
            if (networkElementProvider != null) {
                Collection<INetworkElement> networkElements = networkElementProvider.
                        createNetworkElements(position.getLevel(true), position.getBlockPos());
                for (INetworkElement networkElement : networkElements) {
                    if(!removeNetworkElementPre(networkElement)) {
                        return false;
                    }
                }
                for (INetworkElement networkElement : networkElements) {
                    removeNetworkElementPost(networkElement);
                }
                onNetworkChanged();
                return true;
            }
        } else {
            Thread.dumpStack();
            IntegratedDynamics.clog(org.apache.logging.log4j.Level.WARN, "Tried to remove a path element from a network it was not present in.");
            System.out.println("Cluster: " + baseCluster);
            System.out.println("Tried removing element: " + pathElement);
        }
        return false;
    }

    @Override
    public void afterServerLoad() {
        for (IFullNetworkListener fullNetworkListener : this.fullNetworkListeners) {
            fullNetworkListener.afterServerLoad();
        }
        // All networks start from an invalidated state at server start
        for (INetworkElement element : getElements()) {
            invalidateElement(element);
        }

    }

    @Override
    public void beforeServerStop() {
        for (IFullNetworkListener fullNetworkListener : this.fullNetworkListeners) {
            fullNetworkListener.beforeServerStop();
        }
    }

    @Override
    public Set<INetworkElement> getElements() {
        return this.elements;
    }

    @Override
    public boolean isKilled() {
        return this.killed;
    }

    protected void onNetworkChanged() {
        this.changed = true;
    }

    @Override
    public boolean hasChanged() {
        return this.changed;
    }

    @Override
    public int getCablesCount() {
        return baseCluster.size();
    }

    @Override
    public long getLastSecondDuration(INetworkElement networkElement) {
        Long duration = lastSecondDurations.get(networkElement);
        return duration == null ? 0 : duration;
    }

    @Override
    public void resetLastSecondDurations() {
        lastSecondDurations.clear();
    }

    @Override
    public boolean isCrashed() {
        return crashed;
    }

    @Override
    public void setCrashed(boolean crashed) {
        this.crashed = crashed;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability) {
        return capabilityDispatcher == null ? null : capabilityDispatcher.getCapability(capability, null);
    }

    @Override
    public void invalidateElement(INetworkElement element) {
        for (IFullNetworkListener fullNetworkListener : this.fullNetworkListeners) {
            fullNetworkListener.invalidateElement(element);
        }
        invalidatedElements.add(element);
    }

    @Override
    public void revalidateElement(INetworkElement element) {
        for (IFullNetworkListener fullNetworkListener : this.fullNetworkListeners) {
            fullNetworkListener.revalidateElement(element);
        }
        invalidatedElements.remove(element);
    }

    @Override
    public boolean containsSidedPathElement(ISidedPathElement pathElement) {
        return baseCluster.contains(pathElement);
    }

    @Override
    public IFullNetworkListener[] getFullNetworkListeners() {
        return this.fullNetworkListeners;
    }

    protected boolean isValid(INetworkElement element) {
        if (invalidatedElements.contains(element)) {
            if (element.canRevalidate(this)) {
                element.revalidate(this);
                return true;
            }
            return false;
        }
        return true;
    }
}
