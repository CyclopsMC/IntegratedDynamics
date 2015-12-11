package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.datastructure.CompositeMap;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.block.IVariableContainerFacade;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.*;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.api.network.event.INetworkEventBus;
import org.cyclops.integrateddynamics.api.part.*;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.read.IPartStateReader;
import org.cyclops.integrateddynamics.api.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.core.network.event.NetworkElementAddEvent;
import org.cyclops.integrateddynamics.core.network.event.NetworkElementRemoveEvent;
import org.cyclops.integrateddynamics.core.network.event.NetworkEventBus;
import org.cyclops.integrateddynamics.core.path.CablePathElement;
import org.cyclops.integrateddynamics.core.path.Cluster;
import org.cyclops.integrateddynamics.core.path.PathFinder;
import org.cyclops.integrateddynamics.core.persist.world.NetworkWorldStorage;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

import java.util.*;

/**
 * A network instance that can hold a set of {@link INetworkElement}s.
 * Note that this network only contains references to the relevant data, it does not contain the actual information.
 * @author rubensworks
 */
public class Network implements INetwork {

    private Cluster<CablePathElement> baseCluster;

    private final INetworkEventBus eventBus = new NetworkEventBus();
    private final TreeSet<INetworkElement> elements = Sets.newTreeSet();
    private TreeSet<INetworkElement> updateableElements = null;
    private TreeMap<INetworkElement, Integer> updateableElementsTicks = null;
    private Map<Integer, PartPos> partPositions = Maps.newHashMap();
    private final List<DimPos> variableContainerPositions = Lists.newLinkedList();
    private Map<Integer, IVariableFacade> compositeVariableCache = null;
    private Map<Integer, IValue> lazyExpressionValueCache = Maps.newHashMap();

    private volatile boolean partsChanged = false;
    private volatile boolean killed = false;

    /**
     * This constructor should not be called, except for the process of constructing networks from NBT.
     */
    public Network() {
        this.baseCluster = new Cluster<CablePathElement>();
    }

    /**
     * Create a new network from a given cluster of cables.
     * Each cable will be checked if it is an instance of {@link INetworkElementProvider} and will add all its
     * elements to the network in that case.
     * Each cable that is an instance of {@link IPartContainerFacade}
     * will have the network stored in its part container.
     * @param cables The cables that make up the connections in the network which can potentially provide network
     *               elements.
     */
    public Network(Cluster<CablePathElement> cables) {
        this.baseCluster = cables;
        deriveNetworkElements(baseCluster);
    }

    private void deriveNetworkElements(Cluster<CablePathElement> cables) {
        if(!killIfEmpty()) {
            for (CablePathElement cable : cables) {
                World world = cable.getPosition().getWorld();
                BlockPos pos = cable.getPosition().getBlockPos();
                Block block = world.getBlockState(pos).getBlock();
                if (block instanceof INetworkElementProvider) {
                    for(INetworkElement element : ((INetworkElementProvider) block).createNetworkElements(world, pos)) {
                        addNetworkElement(element, true);
                    }
                }
                if (block instanceof INetworkCarrier) {
                    INetworkCarrier networkCarrier = (INetworkCarrier) block;
                    // Correctly remove any previously saved network in this carrier
                    // and set the new network to this.
                    INetwork network = networkCarrier.getNetwork(world, pos);
                    if (network != null) {
                        if(network.removeCable(block, cable)) {
                            network.notifyPartsChanged();
                        }
                    }
                    networkCarrier.resetCurrentNetwork(world, pos);
                    networkCarrier.setNetwork(this, world, pos);
                }
            }
        }
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
    public boolean addPart(int partId, PartPos partPos) {
        if(partPositions.containsKey(partId)) {
            return false;
        }
        partPositions.put(partId, partPos);
        return true;
    }

    @Override
    public IPartState getPartState(int partId) {
        PartPos partPos = partPositions.get(partId);
        return TileMultipartTicking.get(partPos.getPos()).getPartState(partPos.getSide());
    }

    @Override
    public IPartType getPartType(int partId) {
        PartPos partPos = partPositions.get(partId);
        return TileMultipartTicking.get(partPos.getPos()).getPart(partPos.getSide());
    }

    @Override
    public void removePart(int partId) {
        partPositions.remove(partId);
    }

    @Override
    public boolean hasPart(int partId) {
        if(!partPositions.containsKey(partId)) {
            return false;
        }
        PartPos partPos = partPositions.get(partId);
        IPartContainer partContainer = TileMultipartTicking.get(partPos.getPos());
        return partContainer != null && partContainer.hasPart(partPos.getSide());
    }

    @Override
    public <V extends IValue> boolean hasPartVariable(int partId, IAspectRead<V, ?> aspect) {
        if(!hasPart(partId)) {
            return false;
        }
        IPartState partState = getPartState(partId);
        if(!(partState instanceof IPartStateReader)) {
            return false;
        }
        IPartType partType = getPartType(partId);
        if(!(partType instanceof IPartTypeReader)) {
            return false;
        }
        return ((IPartTypeReader) getPartType(partId)).getVariable(
                PartTarget.fromCenter(partPositions.get(partId)), (IPartStateReader) partState, aspect) != null;
    }

    @Override
    public <V extends IValue> IVariable<V> getPartVariable(int partId, IAspectRead<V, ?> aspect) {
        return ((IPartStateReader) getPartState(partId)).getVariable(aspect);
    }

    protected Map<Integer, IVariableFacade> getVariableCache() {
        if(compositeVariableCache == null) {
            // Create a new composite map view on the existing variable containers in this network.
            CompositeMap<Integer, IVariableFacade> compositeMap = new CompositeMap<>();
            for(Iterator<DimPos> it = variableContainerPositions.iterator(); it.hasNext();) {
                DimPos dimPos = it.next();
                World world = dimPos.getWorld();
                BlockPos pos = dimPos.getBlockPos();
                Block block = world.getBlockState(pos).getBlock();
                if(block instanceof IVariableContainerFacade) {
                    compositeMap.addElement(((IVariableContainerFacade) block).getVariableContainer(world, pos).getVariableCache());
                } else {
                    IntegratedDynamics.clog(Level.ERROR, "The variable container at " + dimPos + " was invalid, skipping.");
                    it.remove();
                }
            }
            compositeVariableCache = compositeMap;
        }
        return compositeVariableCache;
    }

    @Override
    public boolean hasVariableFacade(int variableId) {
        return getVariableCache().containsKey(variableId);
    }

    @Override
    public IVariableFacade getVariableFacade(int variableId) {
        return getVariableCache().get(variableId);
    }

    @Override
    public void setValue(int id, IValue value) {
        lazyExpressionValueCache.put(id, value);
    }

    @Override
    public boolean hasValue(int id) {
        return lazyExpressionValueCache.containsKey(id);
    }

    @Override
    public IValue getValue(int id) {
        return lazyExpressionValueCache.get(id);
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

    @Override
    public boolean equals(Object object) {
        return object instanceof Network && areNetworksEqual(this, (Network) object);
    }

    @Override
    public NBTTagCompound toNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("baseCluster", this.baseCluster.toNBT());
        return tag;
    }

    @Override
    public void fromNBT(NBTTagCompound tag) {
        this.baseCluster.fromNBT(tag.getCompoundTag("baseCluster"));
        deriveNetworkElements(baseCluster);
        initialize(true);
    }

    @Override
    public boolean addVariableContainer(DimPos dimPos) {
        compositeVariableCache = null;
        return variableContainerPositions.add(dimPos);
    }

    @Override
    public void removeVariableContainer(DimPos dimPos) {
        compositeVariableCache = null;
        variableContainerPositions.remove(dimPos);
    }

    @Override
    public boolean addNetworkElement(INetworkElement element, boolean networkPreinit) {
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
                IEventListenableNetworkElement listenableElement = (IEventListenableNetworkElement) element;
                INetworkEventListener<?> listener = listenableElement.getNetworkEventListener();
                if (listener != null && listener.hasEventSubscriptions()) {
                    for (Class<? extends INetworkEvent> eventType : listener.getSubscribedEvents()) {
                        getEventBus().register(listenableElement, eventType);
                    }
                }
            }
            getEventBus().post(new NetworkElementAddEvent.Post(this, element));
            return true;
        }
        return false;
    }

    @Override
    public void addNetworkElementUpdateable(INetworkElement element) {
        if(element.isUpdate()) {
            updateableElements.add(element);
            updateableElementsTicks.put(element, 0);
        }
    }

    @Override
    public boolean removeNetworkElementPre(INetworkElement element) {
        return getEventBus().postCancelable(new NetworkElementRemoveEvent.Pre(this, element));
    }

    @Override
    public void removeNetworkElementPost(INetworkElement element) {
        if (element instanceof IEventListenableNetworkElement) {
            IEventListenableNetworkElement listenableElement = (IEventListenableNetworkElement) element;
            INetworkEventListener<?> listener = listenableElement.getNetworkEventListener();
            if (listener != null && listener.hasEventSubscriptions()) {
                getEventBus().unregister(listenableElement);
            }
        }
        element.beforeNetworkKill(this);
        element.onNetworkRemoval(this);
        elements.remove(element);
        removeNetworkElementUpdateable(element);
        getEventBus().post(new NetworkElementRemoveEvent.Post(this, element));
    }

    @Override
    public void removeNetworkElementUpdateable(INetworkElement element) {
        updateableElements.remove(element);
        updateableElementsTicks.remove(element);
    }

    /**
     * Called when a network is server-loaded or newly created.
     * @param silent If the element should not be notified for the network becoming alive.
     */
    protected void initialize(boolean silent) {
        updateableElements = Sets.newTreeSet();
        updateableElementsTicks = Maps.newTreeMap();
        for(INetworkElement element : elements) {
            addNetworkElementUpdateable(element);
            if(!silent) {
                element.afterNetworkAlive(this);
            }
        }
    }

    @Override
    public void kill() {
        for(INetworkElement element : elements) {
            element.beforeNetworkKill(this);
        }
        killed = true;
    }

    @Override
    public boolean killIfEmpty() {
        if(baseCluster.isEmpty()) {
            kill();
            return true;
        }
        return false;
    }

    @Override
    public void update() {
        if(killIfEmpty() || killed) {
            NetworkWorldStorage.getInstance(IntegratedDynamics._instance).removeInvalidatedNetwork(this);
        } else {
            // Reset lazy variable cache
            lazyExpressionValueCache.clear();

            // Signal parts of any changes
            if (partsChanged) {
                this.partsChanged = false;
                onPartsChanged();
            }

            // Update updateable network elements
            for (INetworkElement element : updateableElements) {
                if (updateableElementsTicks.get(element) <= 0) {
                    updateableElementsTicks.put(element, element.getUpdateInterval());
                    element.update(this);
                }
                updateableElementsTicks.put(element, updateableElementsTicks.get(element) - 1);
            }
        }
    }

    @Override
    public void notifyPartsChanged() {
        this.partsChanged = true;
    }

    private void onPartsChanged() {
        System.out.println("Parts of network " + this + " are changed.");
    }

    @Override
    public boolean removeCable(Block block, CablePathElement cable) {
        if(baseCluster.remove(cable)) {
            if (block instanceof INetworkElementProvider) {
                Collection<INetworkElement> networkElements = ((INetworkElementProvider) block).
                        createNetworkElements(cable.getPosition().getWorld(), cable.getPosition().getBlockPos());
                for (INetworkElement networkElement : networkElements) {
                    if(!removeNetworkElementPre(networkElement)) {
                        return false;
                    }
                }
                for (INetworkElement networkElement : networkElements) {
                    removeNetworkElementPost(networkElement);
                }
                return true;
            }
        } else {
            IntegratedDynamics.clog(Level.WARN, "Tried to remove a cable from a network it was not present in.");
        }
        return false;
    }

    @Override
    public void afterServerLoad() {

    }

    @Override
    public void beforeServerStop() {

    }

    /**
     * Initiate a full network from the given start position.
     * @param connectable The cable to start the network from.
     * @param world The world.
     * @param pos The position.
     * @return The newly formed network.
     */
    public static Network initiateNetworkSetup(ICable<CablePathElement> connectable, World world, BlockPos pos) {
        Network network = new Network(PathFinder.getConnectedCluster(connectable.createPathElement(world, pos)));
        NetworkWorldStorage.getInstance(IntegratedDynamics._instance).addNewNetwork(network);
        return network;
    }

}
