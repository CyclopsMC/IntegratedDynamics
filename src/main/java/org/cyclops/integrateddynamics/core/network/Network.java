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
import org.cyclops.cyclopscore.persist.nbt.INBTSerializable;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.block.IVariableContainerFacade;
import org.cyclops.integrateddynamics.core.block.cable.ICable;
import org.cyclops.integrateddynamics.core.evaluate.expression.LazyExpression;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.item.IVariableFacade;
import org.cyclops.integrateddynamics.core.part.*;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.core.part.read.IPartStateReader;
import org.cyclops.integrateddynamics.core.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.core.path.CablePathElement;
import org.cyclops.integrateddynamics.core.path.Cluster;
import org.cyclops.integrateddynamics.core.path.PathFinder;
import org.cyclops.integrateddynamics.core.persist.world.NetworkWorldStorage;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;

import java.util.*;

/**
 * A network instance that can hold a set of {@link org.cyclops.integrateddynamics.core.network.INetworkElement}s.
 * Note that this network only contains references to the relevant data, it does not contain the actual information.
 * @author rubensworks
 */
public class Network implements INBTSerializable, LazyExpression.IValueCache {

    private Cluster<CablePathElement> baseCluster;

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
     * Each cable that is an instance of {@link org.cyclops.integrateddynamics.core.part.IPartContainerFacade}
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
                    Network network = networkCarrier.getNetwork(world, pos);
                    if (network != null) {
                        network.removeCable(block, cable);
                        network.notifyPartsChanged();
                    }
                    networkCarrier.resetCurrentNetwork(world, pos);
                    networkCarrier.setNetwork(this, world, pos);
                }
            }
        }
    }

    /**
     * Initialize the network element data.
     */
    public void initialize() {
        initialize(false);
    }

    /**
     * Add the given part state to the network.
     * @param partId The id of the part.
     * @param partPos The part position to add.
     * @return If the addition was successful.
     */
    public boolean addPart(int partId, PartPos partPos) {
        if(partPositions.containsKey(partId)) {
            return false;
        }
        partPositions.put(partId, partPos);
        return true;
    }

    /**
     * Get the part state by id from this network.
     * @param partId The part state id.
     * @return The corresponding part state or null.
     */
    public IPartState getPartState(int partId) {
        PartPos partPos = partPositions.get(partId);
        return TileMultipartTicking.get(partPos.getPos()).getPartState(partPos.getSide());
    }

    /**
     * Get the part by id from this network.
     * @param partId The part state id.
     * @return The corresponding part or null.
     */
    public IPartType getPartType(int partId) {
        PartPos partPos = partPositions.get(partId);
        return TileMultipartTicking.get(partPos.getPos()).getPart(partPos.getSide());
    }

    /**
     * Remove the part state by id from this network.
     * @param partId The part state id.
     */
    public void removePart(int partId) {
        partPositions.remove(partId);
    }

    /**
     * Add the position of a variable container.
     * @param dimPos The variable container position.
     * @return If the container did not exist in the network already.
     */
    public boolean addVariableContainer(DimPos dimPos) {
        compositeVariableCache = null;
        return variableContainerPositions.add(dimPos);
    }

    /**
     * Remove the position of a variable container.
     * @param dimPos The variable container position.
     */
    public void removeVariableContainer(DimPos dimPos) {
        compositeVariableCache = null;
        variableContainerPositions.remove(dimPos);
    }

    /**
     * Add a given network element to the network
     * Also checks if it can tick and will handle it accordingly.
     * @param element The network element.
     * @param networkPreinit If the network is still in the process of being initialized.
     * @return If the addition succeeded.
     */
    public boolean addNetworkElement(INetworkElement element, boolean networkPreinit) {
        elements.add(element);
        if(!element.onNetworkAddition(this)) {
            elements.remove(element);
            return false;
        }
        if(!networkPreinit) {
            addNetworkElementUpdateable(element);
        }
        return true;
    }

    /**
     * Add a given network element to the tickable elements set.
     * @param element The network element.
     */
    public void addNetworkElementUpdateable(INetworkElement element) {
        if(element.isUpdate()) {
            updateableElements.add(element);
            updateableElementsTicks.put(element, 0);
        }
    }

    /**
     * Remove a given network element from the network.
     * Also removed its tickable instance.
     * @param element The network element.
     */
    public void removeNetworkElement(INetworkElement element) {
        element.beforeNetworkKill(this);
        element.onNetworkRemoval(this);
        elements.remove(element);
        removeNetworkElementUpdateable(element);
    }

    /**
     * Remove given network element from the tickable elements set.
     * @param element The network element.
     */
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

    /**
     * Terminate the network elements for this network.
     */
    public void kill() {
        for(INetworkElement element : elements) {
            element.beforeNetworkKill(this);
        }
        killed = true;
    }

    /**
     * Kills the network is it had no more network elements.
     * @return If the network was killed.
     */
    public boolean killIfEmpty() {
        if(baseCluster.isEmpty()) {
            kill();
            return true;
        }
        return false;
    }

    /**
     * This network updating should be called each tick.
     */
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

    /**
     * Tell the network to recheck all parts next update round.
     */
    public void notifyPartsChanged() {
        this.partsChanged = true;
    }

    private void onPartsChanged() {
        System.out.println("Parts of network " + this + " are changed.");
    }

    /**
     * Remove the given cable from the network.
     * If the cable had any network elements registered in the network, these will be killed and removed as well.
     * @param block The block instance of the cable element.
     * @param cable The actual cable instance.
     */
    public void removeCable(Block block, CablePathElement cable) {
        if(baseCluster.remove(cable)) {
            if (block instanceof INetworkElementProvider) {
                Collection<INetworkElement> networkElements = ((INetworkElementProvider) block).
                        createNetworkElements(cable.getPosition().getWorld(), cable.getPosition().getBlockPos());
                for (INetworkElement networkElement : networkElements) {
                    removeNetworkElement(networkElement);
                }
            }
        } else {
            IntegratedDynamics.clog(Level.WARN, "Tried to remove a cable from a network it was not present in.");
        }
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

    /**
     * Called when the server loaded this network.
     * This is the time to notify all network elements of this network.
     */
    public void afterServerLoad() {

    }

    /**
     * Called when the server will save this network before stopping.
     * This is the time to notify all network elements of this network.
     */
    public void beforeServerStop() {

    }

    /**
     * Send a refresh to the given network elements types.
     * Used to trigger a refresh in element states.
     * @param type The types of network elements to send an update to.
     */
    public void refresh(Class<? extends INetworkElement> type) {
        for(INetworkElement element : elements) {
            if(type.isInstance(element)) {
                element.refresh(this);
            }
        }
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

    /**
     * Check if this network contains the given part id.
     * @param partId The part state id.
     * @return If this part is present in this network.
     */
    public boolean hasPart(int partId) {
        if(!partPositions.containsKey(partId)) {
            return false;
        }
        PartPos partPos = partPositions.get(partId);
        IPartContainer partContainer = TileMultipartTicking.get(partPos.getPos());
        return partContainer != null && partContainer.hasPart(partPos.getSide());
    }

    /**
     * Check if a variable can be found for a given part and aspect.
     * @param partId The part state id.
     * @param aspect The aspect from the given part.
     * @param <V> The value.
     * @return True if such a variable can be found. False if the given part is not present in the network or if the
     *         given aspect is not present at that part.
     */
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

    /**
     * Get the current variable from the aspect of the given part id.
     * This method can call a NPE or cast exception when the given part does not exists, so make sure to check this before.
     * @param partId The part state id.
     * @param aspect The aspect from the given part.
     * @param <V> The value.
     * @return The variable.
     */
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

    /**
     * Check if this network has access to the variable facade with given variable id.
     * @param variableId The variable id.
     * @return If this network has access to it.
     */
    public boolean hasVariableFacade(int variableId) {
        return getVariableCache().containsKey(variableId);
    }

    /**
     * Get the variable facade with given variable id.
     * @param variableId The variable id.
     * @return The variable facade.
     */
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

}
