package org.cyclops.integrateddynamics.inventory.container;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.inventory.slot.SlotRemoveOnly;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.read.IPartStateReader;
import org.cyclops.integrateddynamics.api.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipartAspects;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;

/**
 * Container for reader parts.
 * @author rubensworks
 */
public class ContainerPartReader<P extends IPartTypeReader<P, S> & IGuiContainerProvider, S extends IPartStateReader<P>>
        extends ContainerMultipartAspects<P, S, IAspectRead> {

    public static final int ASPECT_BOX_HEIGHT = 36;
    private static final int SLOT_IN_X = 96;
    private static final int SLOT_IN_Y = 27;
    private static final int SLOT_OUT_X = 144;
    private static final int SLOT_OUT_Y = 27;

    private final IInventory outputSlots;
    private final BiMap<Integer, IAspectRead> readValueIds = HashBiMap.create();
    private final BiMap<Integer, IAspectRead> readColorIds = HashBiMap.create();

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
            addSlotToContainer(new SlotVariable(inputSlots, i, SLOT_IN_X, SLOT_IN_Y + getAspectBoxHeight() * i));
            disableSlot(i);
        }

        this.outputSlots = new SimpleInventory(getUnfilteredItemCount(), "temporaryOutputSlots", 1);
        for(int i = 0; i < getUnfilteredItemCount(); i++) {
            addSlotToContainer(new SlotRemoveOnly(outputSlots, i, SLOT_OUT_X, SLOT_OUT_Y + getAspectBoxHeight() * i));
            disableSlot(i + getUnfilteredItemCount());
        }

        addPlayerInventory(player.inventory, 9, 131);

        for(IAspectRead aspectRead : getUnfilteredItems()) {
            readValueIds.put(getNextValueId(), aspectRead);
            readColorIds.put(getNextValueId(), aspectRead);
        }
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
                itemstack = inputSlots.removeStackFromSlot(i);
                if (itemstack != null) {
                    player.dropPlayerItemWithRandomChoice(itemstack, false);
                }
                itemstack = outputSlots.removeStackFromSlot(i);
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

                setReadValue(aspectRead, Pair.of(readValue, readValueColor));
            }
        }
    }

    public void setReadValue(IAspectRead aspectRead, Pair<String, Integer> readValue) {
        int valueId = readValueIds.inverse().get(aspectRead);
        int colorId = readColorIds.inverse().get(aspectRead);
        ValueNotifierHelpers.setValue(this, valueId, readValue.getLeft());
        ValueNotifierHelpers.setValue(this, colorId, readValue.getRight());
    }

    public Pair<String, Integer> getReadValue(IAspectRead aspect) {
        int valueId = readValueIds.inverse().get(aspect);
        int colorId = readColorIds.inverse().get(aspect);
        try {
            return Pair.of(ValueNotifierHelpers.getValueString(this, valueId), ValueNotifierHelpers.getValueInt(this, colorId));
        } catch(NullPointerException e) {
            return null;
        }
    }

}
