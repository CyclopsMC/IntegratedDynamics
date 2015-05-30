package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipart;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.IPartType;

/**
 * Container for reader parts.
 * @author rubensworks
 */
public class ContainerPartReader<P extends IPartType<P, S> & IGuiContainerProvider, S extends IPartState<P>>
        extends ContainerMultipart<P, S> {
    /**
     * Make a new instance.
     * @param player        The player.
     * @param partContainer The part container.
     * @param partType      The part type.
     * @param partState     The part state.
     */
    public ContainerPartReader(EntityPlayer player, IPartContainer partContainer, P partType, S partState) {
        super(player, partContainer, partType, partState);
    }

}
