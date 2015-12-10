package org.cyclops.integrateddynamics.inventory.container;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.player.EntityPlayer;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipart;
import org.cyclops.integrateddynamics.core.part.panel.PartTypePanelVariableDriven;

/**
 * Container for writer parts.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class ContainerPartDisplay<P extends PartTypePanelVariableDriven<P, S>, S extends PartTypePanelVariableDriven.State<P, S>> extends ContainerMultipart<P, S> {

    private static final int SLOT_X = 79;
    private static final int SLOT_Y = 8;

    /**
     * Make a new instance.
     * @param target        The target.
     * @param player        The player.
     * @param partContainer The part container.
     * @param partType      The part type.
     */
    public ContainerPartDisplay(EntityPlayer player, PartTarget target, IPartContainer partContainer, IPartType partType) {
        super(player, target, partContainer, (P) partType);

        SimpleInventory inventory = getPartState().getInventory();
        inventory.addDirtyMarkListener(this);

        addInventory(getPartState().getInventory(), 0, 80, 14, 1, 1);
        addPlayerInventory(player.inventory, 8, 46);
    }

    @Override
    public void onDirty() {
        if(!MinecraftHelpers.isClientSide()) {
            getPartState().onVariableContentsUpdated(getPartType(), getTarget());
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        getPartState().getInventory().removeDirtyMarkListener(this);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    @Override
    protected int getSizeInventory() {
        return getPartState().getInventory().getSizeInventory();
    }
}
