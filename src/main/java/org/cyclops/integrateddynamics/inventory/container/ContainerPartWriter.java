package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipart;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.core.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.core.part.write.IPartTypeWriter;

/**
 * Container for writer parts.
 * @author rubensworks
 */
public class ContainerPartWriter<P extends IPartTypeWriter<P, S> & IGuiContainerProvider, S extends IPartStateWriter<P>>
        extends ContainerMultipart<P, S, IAspectWrite> {
    /**
     * Make a new instance.
     * @param partTarget    The target.
     * @param player        The player.
     * @param partContainer The part container.
     * @param partType      The part type.
     * @param partState     The part state.
     */
    public ContainerPartWriter(EntityPlayer player, PartTarget partTarget, IPartContainer partContainer, P partType, S partState) {
        super(player, partTarget, partContainer, partType, partState, partType.getWriteAspects());
        addPlayerInventory(player.inventory, 9, 131);
    }

    @Override
    protected IInventory constructInputSlotsInventory() {
        SimpleInventory inventory = getPartState().getInventory();
        inventory.addDirtyMarkListener(this);
        return inventory;
    }

    @Override
    public void onDirty() {
        getPartType().updateActivation(getTarget(), getPartState());
    }

}
