package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.block.ICableConnectable;
import org.cyclops.integrateddynamics.core.path.CablePathElement;
import org.cyclops.integrateddynamics.core.path.Cluster;
import org.cyclops.integrateddynamics.core.path.PathFinder;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A network instance that can hold a set of {@link org.cyclops.integrateddynamics.core.network.INetworkElement}s.
 * @author rubensworks
 */
public class Network {

    private Set<INetworkElement> elements = Sets.newHashSet();
    private Set<INetworkElement> updateableElements = null;
    private Map<INetworkElement, Integer> updateableElementsTicks = null;

    /**
     * Create a new network for a predefined collection of network elements.
     * @param elements The network elements that make up the network.
     */
    public Network(Collection<INetworkElement> elements) {
        this.elements.addAll(elements);
    }

    /**
     * Create a new network from a given cluster of cables.
     * Each cable will be checked if it is an instance of {@link INetworkElementProvider} and will add all its
     * elements to the network in that case.
     * @param cables The cables that make up the connections in the network which can potentially provide network
     *               elements.
     */
    public Network(Cluster<CablePathElement> cables) {
        for(CablePathElement cable : cables) {
            World world = cable.getPosition().getWorld();
            BlockPos pos = cable.getPosition().getBlockPos();
            Block block = world.getBlockState(pos).getBlock();
            if(block instanceof INetworkElementProvider) {
                elements.addAll(((INetworkElementProvider) block).createNetworkElements(world, pos));
            }
        }
    }

    /**
     * Initialize the network element data.
     */
    public void initialize() {
        updateableElements = Sets.newHashSet();
        updateableElementsTicks = Maps.newHashMap();
        for(INetworkElement element : elements) {
            if(element.isUpdate()) {
                updateableElements.add(element);
                updateableElementsTicks.put(element, 0);
            }
            element.afterNetworkAlive();
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
        for(INetworkElement element : updateableElements) {
            if(updateableElementsTicks.get(element) <= 0) {
                updateableElementsTicks.put(element, element.getUpdateInterval());
                element.update();
            }
            updateableElementsTicks.put(element, updateableElementsTicks.get(element) - 1);
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

    /**
     * Initiate a full network from the given start position.
     * @param connectable The cable to start the network from.
     * @param world The world.
     * @param pos The position.
     * @return The newly formed network.
     */
    public static Network initiateNetworkSetup(ICableConnectable<CablePathElement> connectable, World world, BlockPos pos) {
        return new Network(PathFinder.getConnectedCluster(connectable.createPathElement(world, pos)));
    }

}
