package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.inventory.slot.SlotSingleItem;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipart;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.core.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.core.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.item.ItemVariable;

/**
 * Container for writer parts.
 * @author rubensworks
 */
public class ContainerPartWriter<P extends IPartTypeWriter<P, S> & IGuiContainerProvider, S extends IPartStateWriter<P>>
        extends ContainerMultipart<P, S, IAspectWrite> {

    public static final int ASPECT_BOX_HEIGHT = 18;
    private static final int PAGE_SIZE = 6;
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
        for(int i = 0; i < getUnfilteredItemCount(); i++) {
            addSlotToContainer(new SlotSingleItem(inputSlots, i, 144, 18 + ASPECT_BOX_HEIGHT * i, ItemVariable.getInstance()));
            disableSlot(i);
        }

        addPlayerInventory(player.inventory, 9, 131);
    }

    @Override
    public int getAspectBoxHeight() {
        return ASPECT_BOX_HEIGHT;
    }

    @Override
    public int getPageSize() {
        return PAGE_SIZE;
    }

    @Override
    protected void enableSlot(int slotIndex, int row) {
        Slot slot = getSlot(slotIndex);
        slot.xDisplayPosition = 144;
        slot.yDisplayPosition = 18 + ASPECT_BOX_HEIGHT * row;
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
