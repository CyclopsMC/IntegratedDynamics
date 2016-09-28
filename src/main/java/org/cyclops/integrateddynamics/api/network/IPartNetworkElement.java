package org.cyclops.integrateddynamics.api.network;

import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;

/**
 * A part network element.
 * @author rubensworks
 */
public interface IPartNetworkElement<P extends IPartType<P, S>, S extends IPartState<P>> extends IEventListenableNetworkElement<P> {

    /**
     * @return The part.
     */
    public P getPart();

    /**
     * @return The state for this part.
     */
    public S getPartState();

    /**
     * @return The container in which this part resides.
     */
    public IPartContainer getPartContainer();

    /**
     * @return The target and position of this part.
     */
    public PartTarget getTarget();

    /**
     * Set the priority of this part in the network.
     * @deprecated Should only be called from {@link INetwork#setPriority(INetworkElement, int)}!
     * @param priority The new priority
     */
    @Deprecated
    public void setPriority(int priority);

    /**
     * @return The priority of this part in the network.
     */
    public int getPriority();

}
