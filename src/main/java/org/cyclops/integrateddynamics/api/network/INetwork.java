package org.cyclops.integrateddynamics.api.network;

import org.cyclops.cyclopscore.persist.nbt.INBTSerializable;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.network.event.INetworkEventBus;
import org.cyclops.integrateddynamics.api.path.ICablePathElement;

import java.util.Set;

/**
 * A network can hold a set of {@link INetworkElement}s.
 * Note that this network only contains references to the relevant data, it does not contain the actual information.
 * @param <N> The network type.
 * @author rubensworks
 */
public interface INetwork<N extends INetwork<N>> extends INBTSerializable {

    /**
     * @return The event bus for this network.
     */
    public INetworkEventBus<N> getEventBus();

    /**
     * Add a given network element to the network
     * Also checks if it can tick and will handle it accordingly.
     * @param element The network element.
     * @param networkPreinit If the network is still in the process of being initialized.
     * @return If the addition succeeded.
     */
    public boolean addNetworkElement(INetworkElement<N> element, boolean networkPreinit);

    /**
     * Add a given network element to the tickable elements set.
     * @param element The network element.
     */
    public void addNetworkElementUpdateable(INetworkElement<N> element);

    /**
     * Checks if the given network element can be removed from the network
     * @param element The network element.
     * @return If the element was can be removed from the network.
     */
    public boolean removeNetworkElementPre(INetworkElement<N> element);

    /**
     * Remove a given network element from the network.
     * Also removed its tickable instance.
     * @param element The network element.
     */
    public void removeNetworkElementPost(INetworkElement<N> element);

    /**
     * Remove given network element from the tickable elements set.
     * @param element The network element.
     */
    public void removeNetworkElementUpdateable(INetworkElement<N> element);

    /**
     * Terminate the network elements for this network.
     */
    public void kill();

    /**
     * Kills the network is it had no more network elements.
     * @return If the network was killed.
     */
    public boolean killIfEmpty();

    /**
     * This network updating should be called each tick.
     */
    public void update();

    /**
     * Remove the given cable from the network.
     * If the cable had any network elements registered in the network, these will be killed and removed as well.
     * @param cable The actual cable instance.
     * @param cablePathElement The actual cable instance.
     * @return If the cable was removed.
     */
    public boolean removeCable(ICable cable, ICablePathElement cablePathElement);

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
     * @return The network elements.
     */
    public Set<INetworkElement<N>> getElements();

    /**
     * @return If this network has been killed.
     */
    public boolean isKilled();

    /**
     * @return If the network has changed structure or elements in the last tick.
     */
    public boolean hasChanged();

    /**
     * @return The number of cables in the network.
     */
    public int getCablesCount();

    /**
     * Get the last tick duration of the given network element.
     * @param networkElement The networkelement
     * @return Duration in nanoseconds
     */
    public long getLastSecondDuration(INetworkElement<N> networkElement);

    /**
     * Reset the last second duration counts.
     */
    public void resetLastSecondDurations();

}
