package org.cyclops.integrateddynamics.inventory.container;

import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.inventory.slot.SlotRemoveOnly;
import org.cyclops.cyclopscore.inventory.slot.SlotSingleItem;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipart;
import org.cyclops.integrateddynamics.core.network.packet.PartReaderValuePacket;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.core.part.read.IPartStateReader;
import org.cyclops.integrateddynamics.core.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.item.ItemVariable;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.Map;

/**
 * Container for reader parts.
 * @author rubensworks
 */
public class ContainerPartReader<P extends IPartTypeReader<P, S> & IGuiContainerProvider, S extends IPartStateReader<P>>
        extends ContainerMultipart<P, S, IAspectRead> {

    public static final int ASPECT_BOX_HEIGHT = 36;
    private static final int SLOT_IN_X = 96;
    private static final int SLOT_IN_Y = 27;
    private static final int SLOT_OUT_X = 144;
    private static final int SLOT_OUT_Y = 27;

    private final IInventory outputSlots;
    private Map<IAspectRead, Pair<String, Integer>> readValues = Maps.newHashMap();
    private Map<IAspectRead, String> lastReadValues = Maps.newHashMap();

    /**
     * Make a new instance.
     * @param partTarget    The target.
     * @param player        The player.
     * @param partContainer The part container.
     * @param partType      The part type.
     */
    public ContainerPartReader(EntityPlayer player, PartTarget partTarget, IPartContainer partContainer, P partType) {
        super(player, partTarget, partContainer, partType, partType.getReadAspects());

        for(int i = 0; i < getUnfilteredItemCount(); i++) {
            addSlotToContainer(new SlotSingleItem(inputSlots, i, SLOT_IN_X, SLOT_IN_Y + getAspectBoxHeight() * i, ItemVariable.getInstance()));
            disableSlot(i);
        }

        this.outputSlots = new SimpleInventory(getUnfilteredItemCount(), "temporaryOutputSlots", 1);
        for(int i = 0; i < getUnfilteredItemCount(); i++) {
            addSlotToContainer(new SlotRemoveOnly(outputSlots, i, SLOT_OUT_X, SLOT_OUT_Y + getAspectBoxHeight() * i));
            disableSlot(i + getUnfilteredItemCount());
        }

        addPlayerInventory(player.inventory, 9, 131);
    }

    @Override
    public int getAspectBoxHeight() {
        return ASPECT_BOX_HEIGHT;
    }

    @Override
    protected void enableSlot(int slotIndex, int row) {
        Slot slot = getSlot(slotIndex);
        slot.xDisplayPosition = SLOT_IN_X;
        slot.yDisplayPosition = SLOT_IN_Y + getAspectBoxHeight() * row;
    }

    protected void disableSlotOutput(int slotIndex) {
        Slot slot = getSlot(slotIndex + getUnfilteredItemCount());
        // Yes I know this is ugly.
        // If you are reading this and know a better way, please tell me.
        slot.xDisplayPosition = Integer.MIN_VALUE;
        slot.yDisplayPosition = Integer.MIN_VALUE;
    }

    protected void enableSlotOutput(int slotIndex, int row) {
        Slot slot = getSlot(slotIndex + getUnfilteredItemCount());
        slot.xDisplayPosition = SLOT_OUT_X;
        slot.yDisplayPosition = SLOT_OUT_Y + getAspectBoxHeight() * row;
    }

    @Override
    protected void onScroll() {
        super.onScroll();
        for(int i = 0; i < getUnfilteredItemCount(); i++) {
            disableSlotOutput(i);
        }
    }

    @Override
    protected void enableElementAt(int row, int elementIndex, IAspectRead element) {
        super.enableElementAt(row, elementIndex, element);
        enableSlotOutput(elementIndex, row);
    }

    @Override
    protected int getSizeInventory() {
        return getUnfilteredItemCount() * 2; // Input and output slots per item
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (!getWorld().isRemote) {
            for (int i = 0; i < getUnfilteredItemCount(); ++i) {
                ItemStack itemstack;
                itemstack = inputSlots.getStackInSlotOnClosing(i);
                if (itemstack != null) {
                    player.dropPlayerItemWithRandomChoice(itemstack, false);
                }
                itemstack = outputSlots.getStackInSlotOnClosing(i);
                if (itemstack != null) {
                    player.dropPlayerItemWithRandomChoice(itemstack, false);
                }
            }
        }
    }

    @Override
    public void onDirty() {
        for(int i = 0; i < getUnfilteredItemCount(); i++) {
            ItemStack itemStack = inputSlots.getStackInSlot(i);
            if(itemStack != null && outputSlots.getStackInSlot(i) == null) {
                ItemStack outputStack = writeAspectInfo(!getWorld().isRemote, itemStack.copy(), getUnfilteredItems().get(i));
                outputSlots.setInventorySlotContents(i, outputStack);
                inputSlots.decrStackSize(i, 1);
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        if(!MinecraftHelpers.isClientSide()) {
            for (IAspectRead aspectRead : getUnfilteredItems()) {
                String readValue = "";
                int readValueColor = 0;
                IVariable variable = getPartType().getVariable(getTarget(), getPartState(), aspectRead);
                if(variable != null) {
                    try {
                        IValue value = variable.getValue();
                        readValue = value.getType().toCompactString(value);
                        readValueColor = variable.getType().getDisplayColor();
                    } catch (EvaluationException e) {
                        readValue = "ERROR";
                        readValueColor = Helpers.RGBToInt(255, 0, 0);
                    }
                }

                if(!readValue.equals(lastReadValues.get(aspectRead))) {
                    lastReadValues.put(aspectRead, readValue);
                    IntegratedDynamics._instance.getPacketHandler().sendToPlayer(
                            new PartReaderValuePacket(aspectRead.getUnlocalizedName(), readValue, readValueColor), player);
                }
            }
        }
    }

    public void setReadValue(String aspectName, Pair<String, Integer> readValue) {
        IAspect aspect = Aspects.REGISTRY.getAspect(aspectName);
        if(aspect instanceof IAspectRead) {
            readValues.put((IAspectRead) aspect, readValue);
        }
    }

    public Pair<String, Integer> getReadValue(IAspectRead aspect) {
        return readValues.get(aspect);
    }

}
