package org.cyclops.integrateddynamics.api.network;

import net.minecraftforge.common.capabilities.Capability;
import org.cyclops.cyclopscore.persist.nbt.INBTSerializable;
import org.cyclops.integrateddynamics.api.network.event.INetworkEventBus;
import org.cyclops.integrateddynamics.api.path.ISidedPathElement;

import java.util.Set;

/**
 * A network can hold a set of {@link INetworkElement}s.
 * Note that this network only contains references to the relevant data, it does not contain the actual information.
 * @author rubensworks
 */
public interface INetwork extends IFullNetworkListener, INBTSerializable {

    /**
     * @return If this network and its elements have been fully initialized.
     */
    public boolean isInitialized();

    /**
     * @return The event bus for this network.
     */
    public INetworkEventBus getEventBus();

    /**
     * Add a given network element to the tickable elements set.
     * @param element The network element.
     */
    public void addNetworkElementUpdateable(INetworkElement element);

    /**
     * Remove given network element from the tickable elements set.
     * @param element The network element.
     */
    public void removeNetworkElementUpdateable(INetworkElement element);

    /**
     * Set the priority and channel of the given network element.
     * @param element The network element.
     * @param priority The new priority
     * @param channel The new channel
     */
    public void setPriorityAndChannel(INetworkElement element, int priority, int channel);

    /**
     * Kills the network is it had no more network elements.
     * @return If the network was killed.
     */
    public boolean killIfEmpty();

    /**
     * @return The network elements.
     */
    public Set<INetworkElement> getElements();

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
    public long getLastSecondDuration(INetworkElement networkElement);

    /**
     * Reset the last second duration counts.
     */
    public void resetLastSecondDurations();

    /**
     * @return If this network has crashed.
     */
    public boolean isCrashed();

    /**
     * @param crashed The new crashed field.
     */
    public void setCrashed(boolean crashed);

    /**
     * If this network has the given capability.
     * @param capability The capability to check.
     * @return If this has the given capability/
     */
    public boolean hasCapability(Capability<?> capability);

    /**
     * Get the given capability.
     * @param capability The capability to get.
     * @param <T> The capability type.
     * @return The capability instance.
     */
    public <T> T getCapability(Capability<T> capability);

    /**
     * Invalidate the given element.
     * This should be called when the element's chunk is being unloaded.
     * @param element The network element to invalidate.
     */
    public void invalidateElement(INetworkElement element);

    /**
     * Revalidate the given element.
     * This should be called when the element's chunk is being reloaded.
     * @param element The network element to invalidate.
     */
    public void revalidateElement(INetworkElement element);

    /**
     * @param sidedPathElement A sided path element.
     * @return If this network contains the given sided path element.
     */
    public boolean containsSidedPathElement(ISidedPathElement sidedPathElement);

    /**
     * @return All registered network listeners.
     */
    public IFullNetworkListener[] getFullNetworkListeners();

}
