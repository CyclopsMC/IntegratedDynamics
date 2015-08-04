package org.cyclops.integrateddynamics.inventory.container;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.inventory.slot.SlotSingleItem;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipart;
import org.cyclops.integrateddynamics.core.network.packet.PartWriterValuePacket;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.core.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.core.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.core.tileentity.ITileCableNetwork;
import org.cyclops.integrateddynamics.item.ItemVariable;

/**
 * Container for writer parts.
 * @author rubensworks
 */
public class ContainerPartWriter<P extends IPartTypeWriter<P, S> & IGuiContainerProvider, S extends IPartStateWriter<P>>
        extends ContainerMultipart<P, S, IAspectWrite> {

    public static final int ASPECT_BOX_HEIGHT = 18;
    private static final int PAGE_SIZE = 6;
    private static final int SLOT_X = 131;
    private static final int SLOT_Y = 18;

    private final EntityPlayer player;
    @Getter
    @Setter
    private String writeValue = "";
    @Getter
    @Setter
    private int writeValueColor = 0;

    /**
     * Make a new instance.
     * @param partTarget    The target.
     * @param player        The player.
     * @param partContainer The part container.
     * @param partType      The part type.
     */
    public ContainerPartWriter(EntityPlayer player, PartTarget partTarget, IPartContainer partContainer, P partType) {
        super(player, partTarget, partContainer, partType, partType.getWriteAspects());
        this.player = player;
        for(int i = 0; i < getUnfilteredItemCount(); i++) {
            addSlotToContainer(new SlotSingleItem(inputSlots, i, SLOT_X, SLOT_Y + getAspectBoxHeight() * i, ItemVariable.getInstance()));
            disableSlot(i);
        }

        addPlayerInventory(player.inventory, 9, 140);
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
        slot.xDisplayPosition = SLOT_X;
        slot.yDisplayPosition = SLOT_Y + ASPECT_BOX_HEIGHT * row;
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

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        String lastValue = this.writeValue;

        if(!MinecraftHelpers.isClientSide()) {
            if(getPartContainer() instanceof ITileCableNetwork && getPartState().getActiveAspect() != null &&
                    getPartState().getErrors(getPartState().getActiveAspect()).isEmpty()) {
                IVariable variable = getPartState().getVariable(((ITileCableNetwork) getPartContainer()).getNetwork());
                if (variable != null) {
                    try {
                        IValue value = variable.getValue();
                        writeValue = value.getType().toCompactString(value);
                        writeValueColor = variable.getType().getDisplayColor();
                    } catch (EvaluationException e) {
                        writeValue = "ERROR";
                        writeValueColor = Helpers.RGBToInt(255, 0, 0);
                    }
                }
            } else {
                writeValue = "";
            }
        }

        if (!lastValue.equals(writeValue)) {
            IntegratedDynamics._instance.getPacketHandler().sendToPlayer(
                    new PartWriterValuePacket(writeValue, writeValueColor), player);
        }
    }

}
