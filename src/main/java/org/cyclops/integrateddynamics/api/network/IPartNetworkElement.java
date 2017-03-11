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
     * @return If this part's position is currently loaded in the world.
     */
    public boolean isLoaded();

}
