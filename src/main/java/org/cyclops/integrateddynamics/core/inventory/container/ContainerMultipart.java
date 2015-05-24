package org.cyclops.integrateddynamics.core.inventory.container;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.cyclopscore.inventory.container.ExtendedInventoryContainer;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.IPartType;

/**
 * Container for parts.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public abstract class ContainerMultipart<P extends IPartType<P, S> & IGuiContainerProvider, S extends IPartState<P>>
        extends ExtendedInventoryContainer {

    private final IPartContainer partContainer;
    private final P partType;
    private final S partState;

    /**
     * Make a new instance.
     * @param inventory The player inventory.
     * @param partContainer The part container.
     * @param partType The part type.
     * @param partState The part state.
     */
    public ContainerMultipart(InventoryPlayer inventory, IPartContainer partContainer, P partType, S partState) {
        super(inventory, partType);
        this.partContainer = partContainer;
        this.partType = partType;
        this.partState = partState;
    }

    @Override
    protected int getSizeInventory() {
        return 0;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

}
