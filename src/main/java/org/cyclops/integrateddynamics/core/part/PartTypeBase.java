package org.cyclops.integrateddynamics.core.part;

import net.minecraft.util.EnumFacing;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.core.network.INetworkElement;
import org.cyclops.integrateddynamics.core.network.PartNetworkElement;

/**
 * An abstract {@link org.cyclops.integrateddynamics.core.part.IPartType} with a default implementation for creating
 * network elements.
 * @author rubensworks
 */
public abstract class PartTypeBase<P extends IPartType<P, S>, S extends IPartState<P>> implements IPartType<P, S> {

    /**
     * Create a network element for this part type.
     * @param partContainerFacade The facade for reaching the container this part is/will be part of.
     * @param pos The position this network element is/will be placed at.
     * @param side The side this network element is/will be placed at.
     * @return A new network element instance.
     */
    public INetworkElement createNetworkElement(IPartContainerFacade partContainerFacade, DimPos pos, EnumFacing side) {
        return new PartNetworkElement(this, partContainerFacade, pos, side);
    }

}
