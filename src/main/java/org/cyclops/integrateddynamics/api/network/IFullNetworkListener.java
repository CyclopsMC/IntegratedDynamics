package org.cyclops.integrateddynamics.api.network;

import net.minecraft.util.EnumFacing;
import org.cyclops.integrateddynamics.api.path.IPathElement;

/**
 * This should be implemented on network capabilities that wish to listen to all network events.
 * @author rubensworks
 */
public interface IFullNetworkListener {

    /**
     * Add a given network element to the network
     * Also checks if it can tick and will handle it accordingly.
     * @param element The network element.
     * @param networkPreinit If the network is still in the process of being initialized.
     * @return If the addition succeeded.
     */
    public boolean addNetworkElement(INetworkElement element, boolean networkPreinit);

    /**
     * Checks if the given network element can be removed from the network
     * @param element The network element.
     * @return If the element was can be removed from the network.
     */
    public boolean removeNetworkElementPre(INetworkElement element);

    /**
     * Remove a given network element from the network.
     * Also removed its tickable instance.
     * @param element The network element.
     */
    public void removeNetworkElementPost(INetworkElement element);

    /**
     * Terminate the network elements for this network.
     */
    public void kill();

    /**
     * This network updating should be called each tick.
     */
    public void update();

    /**
     * Remove the given path element from the network.
     * If the path element had any network elements registered in the network, these will be killed and removed as well.
     * @param pathElement The path element.
     * @param side The side.
     * @return If the path element was removed.
     */
    public boolean removePathElement(IPathElement pathElement, EnumFacing side);

    /**
     * Called when the server loaded this network.
     * This is the time to notify all network elements of this network.
     */
    public void afterServerLoad();

    /**
     * Called when the server will save this network before stopping.
     * This is the time to notify all network elements of this network.
     */
    public void beforeServerStop();

    /**
     * If the given element can update.
     * @param element The network element.
     * @return If it can update.
     */
    public boolean canUpdate(INetworkElement element);

    /**
     * Called after a network element's update was called.
     * @param element The network element.
     */
    public void postUpdate(INetworkElement element);

    /**
     * When the given element is not being updated because {@link INetwork#canUpdate(INetworkElement)}
     * returned false.
     * @param element The element that is not being updated.
     */
    public void onSkipUpdate(INetworkElement element);

    /**
     * Invalidate the given element.
     * Called when the element's chunk is being unloaded.
     * @param element The network element to invalidate.
     */
    public void invalidateElement(INetworkElement element);

    /**
     * Revalidate the given element.
     * Called when the element's chunk is being reloaded.
     * @param element The network element to invalidate.
     */
    public void revalidateElement(INetworkElement element);

}
