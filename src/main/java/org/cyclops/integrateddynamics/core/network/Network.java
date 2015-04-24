package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.persist.nbt.INBTSerializable;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.block.ICableConnectable;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.IPartContainerFacade;
import org.cyclops.integrateddynamics.core.path.CablePathElement;
import org.cyclops.integrateddynamics.core.path.Cluster;
import org.cyclops.integrateddynamics.core.path.PathFinder;
import org.cyclops.integrateddynamics.core.persist.world.NetworkWorldStorage;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A network instance that can hold a set of {@link org.cyclops.integrateddynamics.core.network.INetworkElement}s.
 * @author rubensworks
 */
public class Network implements INBTSerializable {

    private Cluster<CablePathElement> baseCluster;

    private final Set<INetworkElement> elements = Sets.newHashSet();
    private Set<INetworkElement> updateableElements = null;
    private Map<INetworkElement, Integer> updateableElementsTicks = null;
    private int networkId;

    private boolean partsChanged = false;

    /**
     * This constructor should not be called, except for the process of constructing networks from NBT.
     */
    public Network() {

    }

    /**
     * Create a new network for a predefined collection of network elements.
     * @param elements The network elements that make up the network.
     * @param networkId The unique network ID.
     */
    private Network(Collection<INetworkElement> elements, int networkId) {
        this.elements.addAll(elements);
        this.networkId = networkId;
    }

    /**
     * Create a new network from a given cluster of cables.
     * Each cable will be checked if it is an instance of {@link INetworkElementProvider} and will add all its
     * elements to the network in that case.
     * Each cable that is an instance of {@link org.cyclops.integrateddynamics.core.part.IPartContainerFacade}
     * will have the network stored in its part container.
     * @param cables The cables that make up the connections in the network which can potentially provide network
     *               elements.
     * @param networkId The unique network ID.
     */
    public Network(Cluster<CablePathElement> cables, int networkId) {
        this.baseCluster = cables;
        this.networkId = networkId;
        deriveNetworkElements(baseCluster);
    }

    private void deriveNetworkElements(Cluster<CablePathElement> cables) {
        for(CablePathElement cable : cables) {
            World world = cable.getPosition().getWorld();
            BlockPos pos = cable.getPosition().getBlockPos();
            Block block = world.getBlockState(pos).getBlock();
            if(block instanceof INetworkElementProvider) {
                elements.addAll(((INetworkElementProvider) block).createNetworkElements(world, pos));
            }
            if(block instanceof IPartContainerFacade) {
                IPartContainer partContainer = ((IPartContainerFacade) block).getPartContainer(world, pos);
                Network network = partContainer.getNetwork();
                if(network != null) {
                    network.removeCable(block, cable);
                    network.notifyPartsChanged();
                }
                partContainer.resetCurrentNetwork();
                partContainer.setNetwork(this);
            }
        }
    }

    /**
     * Initialize the network element data.
     */
    public void initialize() {
        initialize(false);
    }

    protected void initialize(boolean silent) {
        updateableElements = Sets.newHashSet();
        updateableElementsTicks = Maps.newHashMap();
        for(INetworkElement element : elements) {
            if(element.isUpdate()) {
                updateableElements.add(element);
                updateableElementsTicks.put(element, 0);
            }
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
    }

    /**
     * This network updating should be called each tick.
     */
    public void update() {
        if(partsChanged) {
            this.partsChanged = false;
            onPartsChanged();
        }

        for(INetworkElement element : updateableElements) {
            if(updateableElementsTicks.get(element) <= 0) {
                updateableElementsTicks.put(element, element.getUpdateInterval());
                element.update();
            }
            updateableElementsTicks.put(element, updateableElementsTicks.get(element) - 1);
        }
    }

    /**
     * Tell the network to recheck all parts next update round.
     */
    public void notifyPartsChanged() {
        this.partsChanged = true;
    }

    private void onPartsChanged() {
        //deriveNetworkElements(baseCluster);
        //initialize(true);
        System.out.println("Parts of network " + networkId + " are changed.");
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
                networkElement.beforeNetworkKill();
                elements.remove(networkElement);
                updateableElements.remove(networkElement);
                updateableElementsTicks.remove(networkElement);
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
        tag.setInteger("id", this.networkId);
        tag.setTag("baseCluster", this.baseCluster.toNBT());
        return tag;
    }

    @Override
    public void fromNBT(NBTTagCompound tag) {
        this.baseCluster = new Cluster<CablePathElement>();
        this.baseCluster.fromNBT(tag.getCompoundTag("baseCluster"));
        this.networkId = tag.getInteger("id");
        deriveNetworkElements(baseCluster);
        initialize(true);
    }

    /**
     * Initiate a full network from the given start position.
     * @param connectable The cable to start the network from.
     * @param world The world.
     * @param pos The position.
     * @return The newly formed network.
     */
    public static Network initiateNetworkSetup(ICableConnectable<CablePathElement> connectable, World world, BlockPos pos) {
        int nextId = IntegratedDynamics._instance.getGlobalCounters().getNext("network");
        System.out.println("Next network id: " + nextId);
        Network network = new Network(PathFinder.getConnectedCluster(connectable.createPathElement(world, pos)), nextId);
        NetworkWorldStorage.getInstance(IntegratedDynamics._instance).addNewNetwork(network);
        return network;
    }

}
