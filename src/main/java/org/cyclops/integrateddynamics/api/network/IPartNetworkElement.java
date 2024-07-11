package org.cyclops.integrateddynamics.api.network;

import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.PartStateException;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;

/**
 * A part network element.
 * @author rubensworks
 */
public interface IPartNetworkElement<P extends IPartType<P, S>, S extends IPartState<P>> extends
        IEventListenableNetworkElement<P>, IPositionedNetworkElement, ISidedNetworkElement, IIdentifiableNetworkElement {

    public static final ResourceLocation GROUP = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "part");

    /**
     * @return The part.
     */
    public P getPart();

    /**
     * @return The state for this part.
     * @throws PartStateException If the part state could not be found.
     */
    public S getPartState() throws PartStateException;

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
