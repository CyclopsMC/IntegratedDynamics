package org.cyclops.integrateddynamics.inventory.container;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.inventory.slot.SlotRemoveOnly;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.PartStateException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.read.IPartStateReader;
import org.cyclops.integrateddynamics.api.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipartAspects;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;
import org.cyclops.integrateddynamics.core.part.event.PartReaderAspectEvent;

import java.util.Optional;

/**
 * Container for reader parts.
 * @author rubensworks
 */
public class ContainerPartReader<P extends IPartTypeReader<P, S>, S extends IPartStateReader<P>>
        extends ContainerMultipartAspects<P, S, IAspectRead> {

    public static final int ASPECT_BOX_HEIGHT = 36;
    private static final int SLOT_IN_X = 96;
    private static final int SLOT_IN_Y = 27;
    private static final int SLOT_OUT_X = 144;
    private static final int SLOT_OUT_Y = 27;

    private final IInventory outputSlots;
    private final BiMap<Integer, IAspectRead> readValueIds = HashBiMap.create();
    private final BiMap<Integer, IAspectRead> readColorIds = HashBiMap.create();

    public ContainerPartReader(int id, PlayerInventory playerInventory, PacketBuffer packetBuffer) {
        this(id, playerInventory, new SimpleInventory(packetBuffer.readInt(), 1),
                Optional.empty(), Optional.empty(), readPart(packetBuffer));
    }

    public ContainerPartReader(int id, PlayerInventory playerInventory, IInventory inventory,
                                Optional<PartTarget> target, Optional<IPartContainer> partContainer, P partType) {
        super(RegistryEntries.CONTAINER_PART_READER, id, playerInventory, inventory, target, partContainer, partType,
                partType.getReadAspects());

        for(int i = 0; i < getUnfilteredItemCount(); i++) {
            addSlot(new SlotVariable(inputSlots, i, SLOT_IN_X, SLOT_IN_Y + getAspectBoxHeight() * i));
            disableSlot(i);
        }

        this.outputSlots = new SimpleInventory(getUnfilteredItemCount(), 1);
        for(int i = 0; i < getUnfilteredItemCount(); i++) {
            addSlot(new SlotRemoveOnly(outputSlots, i, SLOT_OUT_X, SLOT_OUT_Y + getAspectBoxHeight() * i));
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
        slot.xPos = SLOT_IN_X;
        slot.yPos = SLOT_IN_Y + getAspectBoxHeight() * row;
    }

    protected void disableSlotOutput(int slotIndex) {
        Slot slot = getSlot(slotIndex + getUnfilteredItemCount());
        // Yes I know this is ugly.
        // If you are reading this and know a better way, please tell me.
        slot.xPos = Integer.MIN_VALUE;
        slot.yPos = Integer.MIN_VALUE;
    }

    protected void enableSlotOutput(int slotIndex, int row) {
        Slot slot = getSlot(slotIndex + getUnfilteredItemCount());
        slot.xPos = SLOT_OUT_X;
        slot.yPos = SLOT_OUT_Y + getAspectBoxHeight() * row;
    }

    @Override
    public void onScroll(int firstRow) {
        super.onScroll(firstRow);
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
    public void onContainerClosed(PlayerEntity player) {
        super.onContainerClosed(player);
        if (!player.world.isRemote()) {
            for (int i = 0; i < getUnfilteredItemCount(); ++i) {
                ItemStack itemStack;
                itemStack = inputSlots.removeStackFromSlot(i);
                if (!itemStack.isEmpty()) {
                    player.dropItem(itemStack, false);
                }
                itemStack = outputSlots.removeStackFromSlot(i);
                if (!itemStack.isEmpty()) {
                    player.dropItem(itemStack, false);
                }
            }
        }
    }

    @Override
    public void onDirty() {
        for(int i = 0; i < getUnfilteredItemCount(); i++) {
            ItemStack itemStack = inputSlots.getStackInSlot(i);
            if(!itemStack.isEmpty() && outputSlots.getStackInSlot(i).isEmpty()) {
                ItemStack outputStack = writeAspectInfo(!player.world.isRemote(), itemStack.copy(), getUnfilteredItems().get(i));
                outputSlots.setInventorySlotContents(i, outputStack);
                inputSlots.decrStackSize(i, 1);
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        try {
            if (!player.world.isRemote()) {
                for (IAspectRead aspectRead : getUnfilteredItems()) {
                    Pair<ITextComponent, Integer> readValue;
                    if(getPartState().get().isEnabled()) {
                        IVariable variable = getPartType().getVariable(getTarget().get(), getPartState().get(), aspectRead);
                        readValue = ValueHelpers.getSafeReadableValue(variable);
                    } else {
                        readValue = Pair.of(new StringTextComponent("NO POWER"), 0);
                    }

                    setReadValue(aspectRead, readValue);
                }
            }
        } catch (PartStateException e) {
            player.closeScreen();
        }
    }

    public void setReadValue(IAspectRead aspectRead, Pair<ITextComponent, Integer> readValue) {
        int valueId = readValueIds.inverse().get(aspectRead);
        int colorId = readColorIds.inverse().get(aspectRead);
        ValueNotifierHelpers.setValue(this, valueId, readValue.getLeft());
        ValueNotifierHelpers.setValue(this, colorId, readValue.getRight());
    }

    public Pair<ITextComponent, Integer> getReadValue(IAspectRead aspect) {
        int valueId = readValueIds.inverse().get(aspect);
        int colorId = readColorIds.inverse().get(aspect);
        try {
            return Pair.of(ValueNotifierHelpers.getValueTextComponent(this, valueId), ValueNotifierHelpers.getValueInt(this, colorId));
        } catch(NullPointerException e) {
            return null;
        }
    }

    @Override
    public ItemStack writeAspectInfo(boolean generateId, ItemStack itemStack, final IAspect aspect) {
        ItemStack resultStack = super.writeAspectInfo(generateId, itemStack, aspect);
        PartTarget target = getTarget().get();
        INetwork network = NetworkHelpers.getNetworkChecked(target.getCenter().getPos().getWorld(true),
                target.getCenter().getPos().getBlockPos(), target.getCenter().getSide());
        IPartNetwork partNetwork = NetworkHelpers.getPartNetworkChecked(network);
        PartReaderAspectEvent event = new PartReaderAspectEvent<>(network, partNetwork, target, getPartType(),
                getPartState().get(), player, (IAspectRead) aspect, resultStack);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getItemStack();
    }

}
