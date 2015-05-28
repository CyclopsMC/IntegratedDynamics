package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.persist.nbt.INBTSerializable;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.block.ICableConnectable;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.IPartContainerFacade;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.path.CablePathElement;
import org.cyclops.integrateddynamics.core.path.Cluster;
import org.cyclops.integrateddynamics.core.path.PathFinder;
import org.cyclops.integrateddynamics.core.persist.world.NetworkWorldStorage;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A network instance that can hold a set of {@link org.cyclops.integrateddynamics.core.network.INetworkElement}s.
 * @author rubensworks
 */
public class Network implements INBTSerializable {

    private Cluster<CablePathElement> baseCluster;

    private final TreeSet<INetworkElement> elements = Sets.newTreeSet();
    private TreeSet<INetworkElement> updateableElements = null;
    private TreeMap<INetworkElement, Integer> updateableElementsTicks = null;
    private Map<Integer, IPartState> partStates = Maps.newHashMap();

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
                    elements.addAll(((INetworkElementProvider) block).createNetworkElements(world, pos));
                }
                if (block instanceof IPartContainerFacade) {
                    IPartContainer partContainer = ((IPartContainerFacade) block).getPartContainer(world, pos);

                    // Correctly remove any previously saved network in this partcontainer
                    // and set the new network to this.
                    Network network = partContainer.getNetwork();
                    if (network != null) {
                        network.removeCable(block, cable);
                        network.notifyPartsChanged();
                    }
                    partContainer.resetCurrentNetwork();
                    partContainer.setNetwork(this);

                    // Capture all parts in this container
                    for(EnumFacing side : partContainer.getParts().keySet()) {
                        addPart(partContainer.getPartState(side));
                    }
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
     * @param partState The part state to add.
     * @return If the addition was successful.
     */
    public boolean addPart(IPartState partState) {
        if(partStates.containsKey(partState.getId())) {
            return false;
        }
        partStates.put(partState.getId(), partState);
        return true;
    }

    /**
     * Get the part state by id from this network.
     * @param partId The part state id.
     * @return The corresponding part state or null.
     */
    public IPartState getPart(int partId) {
        return partStates.get(partId);
    }

    /**
     * Remove the part state by id from this network.
     * @param partId The part state id.
     */
    public void removePart(int partId) {
        partStates.remove(partId);
    }

    /**
     * Add a given network element to the network
     * Also checks if it can tick and will handle it accordingly.
     * @param element The network element.
     */
    public void addNetworkElement(INetworkElement element) {
        elements.add(element);
        addNetworkElementUpdateable(element);
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
        element.beforeNetworkKill();
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
                element.afterNetworkAlive();
            }
        }
    }

    /**
     * Terminate the network elements for this network.
     */
    public void kill() {
        for(INetworkElement element : elements) {
            element.beforeNetworkKill();
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
            if (partsChanged) {
                this.partsChanged = false;
                onPartsChanged();
            }
            for (INetworkElement element : updateableElements) {
                if (updateableElementsTicks.get(element) <= 0) {
                    updateableElementsTicks.put(element, element.getUpdateInterval());
                    element.update();
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
        baseCluster.remove(cable);
        if(block instanceof INetworkElementProvider) {
            Collection<INetworkElement> networkElements = ((INetworkElementProvider) block).
                    createNetworkElements(cable.getPosition().getWorld(), cable.getPosition().getBlockPos());
            for(INetworkElement networkElement : networkElements) {
                removeNetworkElement(networkElement);
            }
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
     * Initiate a full network from the given start position.
     * @param connectable The cable to start the network from.
     * @param world The world.
     * @param pos The position.
     * @return The newly formed network.
     */
    public static Network initiateNetworkSetup(ICableConnectable<CablePathElement> connectable, World world, BlockPos pos) {
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
        return partStates.containsKey(partId);
    }

    /**
     * Check if a variable can be found for a given part and aspect.
     * @param partId The part state id.
     * @param aspect The aspect from the given part.
     * @param <V> The value.
     * @return True if such a variable can be found. False if the given part is not present in the network or if the
     *         given aspect is not present at that part.
     */
    public <V extends IValue> boolean hasVariable(int partId, IAspect<V, ?> aspect) {
        return hasPart(partId) && getPart(partId).getVariable(aspect) != null;
    }

    /**
     * Get the current variable from the aspect of the given part id.
     * This method can call a NPE when the given part does not exists, so make sure to check this before.
     * @param partId The part state id.
     * @param aspect The aspect from the given part.
     * @param <V> The value.
     * @return The variable.
     */
    public <V extends IValue> IVariable<V> getVariable(int partId, IAspect<V, ?> aspect) {
        return getPart(partId).getVariable(aspect);
    }

}
