package org.cyclops.integrateddynamics.inventory.container;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
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

    private final Container outputSlots;
    private final BiMap<Integer, IAspectRead> readValueIds = HashBiMap.create();
    private final BiMap<Integer, IAspectRead> readColorIds = HashBiMap.create();

    public ContainerPartReader(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(id, playerInventory, new SimpleInventory(0, 1),
                PartHelpers.readPartTarget(packetBuffer), Optional.empty(), PartHelpers.readPart(packetBuffer));
    }

    public ContainerPartReader(int id, Inventory playerInventory, Container inventory,
                                PartTarget target, Optional<IPartContainer> partContainer, P partType) {
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

        addPlayerInventory(player.getInventory(), 9, 131);

        for(IAspectRead aspectRead : getUnfilteredItems()) {
            readValueIds.put(getNextValueId(), aspectRead);
            readColorIds.put(getNextValueId(), aspectRead);
        }
    }

    @Override
    protected boolean isAssertInventorySize() {
        return false;
    }

    @Override
    protected int getSizeInventory() {
        return getPartType().getReadAspects().size() * 2;
    }

    @Override
    public int getAspectBoxHeight() {
        return ASPECT_BOX_HEIGHT;
    }

    @Override
    protected void enableSlot(int slotIndex, int row) {
        Slot slot = getSlot(slotIndex);
        setSlotPosX(slot, SLOT_IN_X);
        setSlotPosY(slot, SLOT_IN_Y + getAspectBoxHeight() * row);
    }

    protected void disableSlotOutput(int slotIndex) {
        Slot slot = getSlot(slotIndex + getUnfilteredItemCount());
        // Yes I know this is ugly.
        // If you are reading this and know a better way, please tell me.
        setSlotPosX(slot, Integer.MIN_VALUE);
        setSlotPosY(slot, Integer.MIN_VALUE);
    }

    protected void enableSlotOutput(int slotIndex, int row) {
        Slot slot = getSlot(slotIndex + getUnfilteredItemCount());
        setSlotPosX(slot, SLOT_OUT_X);
        setSlotPosY(slot, SLOT_OUT_Y + getAspectBoxHeight() * row);
    }

    @Override
    public void onScroll(int firstRow) {
        for(int i = 0; i < getUnfilteredItemCount(); i++) {
            disableSlotOutput(i);
        }
        super.onScroll(firstRow);
    }

    @Override
    protected void enableElementAt(int row, int elementIndex, IAspectRead element) {
        super.enableElementAt(row, elementIndex, element);
        enableSlotOutput(elementIndex, row);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            for (int i = 0; i < getUnfilteredItemCount(); ++i) {
                ItemStack itemStack;
                itemStack = inputSlots.removeItemNoUpdate(i);
                if (!itemStack.isEmpty()) {
                    player.drop(itemStack, false);
                }
                itemStack = outputSlots.removeItemNoUpdate(i);
                if (!itemStack.isEmpty()) {
                    player.drop(itemStack, false);
                }
            }
        }
    }

    @Override
    public void onDirty() {
        for(int i = 0; i < getUnfilteredItemCount(); i++) {
            ItemStack itemStack = inputSlots.getItem(i);
            if(!itemStack.isEmpty() && outputSlots.getItem(i).isEmpty()) {
                ItemStack outputStack = writeAspectInfo(!player.level().isClientSide(), itemStack.copy(), player.level(), getUnfilteredItems().get(i));
                outputSlots.setItem(i, outputStack);
                inputSlots.removeItem(i, 1);
            }
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        try {
            if (!player.level().isClientSide()) {
                for (IAspectRead aspectRead : getUnfilteredItems()) {
                    Pair<MutableComponent, Integer> readValue;
                    if(getPartState().isEnabled()) {
                        IVariable variable = getPartType().getVariable(getTarget(), getPartState(), aspectRead);
                        readValue = ValueHelpers.getSafeReadableValue(variable);
                    } else {
                        readValue = Pair.of(Component.literal("NO POWER"), 0);
                    }

                    setReadValue(aspectRead, readValue);
                }
            }
        } catch (PartStateException e) {
            player.closeContainer();
        }
    }

    public void setReadValue(IAspectRead aspectRead, Pair<MutableComponent, Integer> readValue) {
        int valueId = readValueIds.inverse().get(aspectRead);
        int colorId = readColorIds.inverse().get(aspectRead);
        ValueNotifierHelpers.setValue(this, valueId, readValue.getLeft());
        ValueNotifierHelpers.setValue(this, colorId, readValue.getRight());
    }

    public Pair<Component, Integer> getReadValue(IAspectRead aspect) {
        int valueId = readValueIds.inverse().get(aspect);
        int colorId = readColorIds.inverse().get(aspect);
        try {
            return Pair.of(ValueNotifierHelpers.getValueTextComponent(this, valueId), ValueNotifierHelpers.getValueInt(this, colorId));
        } catch(NullPointerException e) {
            return null;
        }
    }

    @Override
    public ItemStack writeAspectInfo(boolean generateId, ItemStack itemStack, Level level, final IAspect aspect) {
        ItemStack resultStack = super.writeAspectInfo(generateId, itemStack, level, aspect);
        if (player.level().isClientSide()) {
            return resultStack;
        }

        PartTarget target = getTarget();
        INetwork network = NetworkHelpers.getNetworkChecked(target.getCenter().getPos().getLevel(true),
                target.getCenter().getPos().getBlockPos(), target.getCenter().getSide());
        IPartNetwork partNetwork = NetworkHelpers.getPartNetworkChecked(network);
        PartReaderAspectEvent event = new PartReaderAspectEvent<>(network, partNetwork, target, getPartType(),
                getPartState(), player, (IAspectRead) aspect, resultStack);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getItemStack();
    }

}
