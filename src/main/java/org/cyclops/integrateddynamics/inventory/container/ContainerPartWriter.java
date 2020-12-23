package org.cyclops.integrateddynamics.inventory.container;

import com.google.common.collect.Maps;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.PartStateException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipartAspects;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Container for writer parts.
 * @author rubensworks
 */
public class ContainerPartWriter<P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>>
        extends ContainerMultipartAspects<P, S, IAspectWrite> {

    public static final int ASPECT_BOX_HEIGHT = 18;
    private static final int PAGE_SIZE = 6;
    private static final int SLOT_X = 131;
    private static final int SLOT_Y = 18;

    private final int valueId, colorId, enabledId, activeAspectId;
    private final Map<IAspectWrite, Integer> aspectErrorIds;

    public ContainerPartWriter(int id, PlayerInventory playerInventory, PacketBuffer packetBuffer) {
        this(id, playerInventory, new SimpleInventory(packetBuffer.readInt(), 1),
                PartHelpers.readPartTarget(packetBuffer), Optional.empty(), PartHelpers.readPart(packetBuffer));
    }

    public ContainerPartWriter(int id, PlayerInventory playerInventory, IInventory inventory,
                               PartTarget target, Optional<IPartContainer> partContainer, P partType) {
        super(RegistryEntries.CONTAINER_PART_WRITER, id, playerInventory, inventory, target, partContainer, partType,
                partType.getWriteAspects());
        for(int i = 0; i < getUnfilteredItemCount(); i++) {
            addSlot(new SlotVariable(inputSlots, i, SLOT_X, SLOT_Y + getAspectBoxHeight() * i));
            disableSlot(i);
        }

        addPlayerInventory(player.inventory, 9, 140);

        this.valueId = getNextValueId();
        this.colorId = getNextValueId();
        this.enabledId = getNextValueId();
        this.activeAspectId = getNextValueId();
        this.aspectErrorIds = Maps.newIdentityHashMap();
        for (IAspectWrite aspect : partType.getWriteAspects()) {
            this.aspectErrorIds.put(aspect, getNextValueId());
        }
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
        setSlotPosX(slot, SLOT_X);
        setSlotPosY(slot, SLOT_Y + ASPECT_BOX_HEIGHT * row);
    }

    @Override
    protected IInventory constructInputSlotsInventory() {
        if (!player.world.isRemote()) {
            SimpleInventory inventory = getPartState().getInventory();
            inventory.addDirtyMarkListener(this);
            return inventory;
        } else {
            return super.constructInputSlotsInventory();
        }
    }

    @Override
    public void onDirty() {
        if (!player.world.isRemote()) {
            getPartType().updateActivation(getTarget(), getPartState(), player);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        try {
            if (!player.world.isRemote()) {
                // Update write value
                Pair<IFormattableTextComponent, Integer> readValue;
                S partState = getPartState();
                if (!partState.isEnabled()) {
                    readValue = Pair.of(new StringTextComponent("NO POWER"), 0);
                } else if (partState.hasVariable()) {
                    IPartContainer partContainer = getPartContainer();
                    LazyOptional<INetwork> optionalNetwork = NetworkHelpers.getNetwork(partContainer.getPosition().getWorld(true),
                            partContainer.getPosition().getBlockPos(), getTarget().getCenter().getSide());
                    IPartNetwork partNetwork = optionalNetwork.map(NetworkHelpers::getPartNetworkChecked).orElse(null);
                    if (partNetwork != null) {
                        IVariable variable = partState.getVariable(optionalNetwork.orElse(null), partNetwork);
                        readValue = ValueHelpers.getSafeReadableValue(variable);
                    } else {
                        readValue = Pair.of(new StringTextComponent("NETWORK CORRUPTED!"), Helpers.RGBToInt(255, 100, 0));
                    }
                } else {
                    readValue = Pair.of(new StringTextComponent(""), 0);
                }
                setWriteValue(readValue.getLeft(), readValue.getRight());

                // Update error values
                for (IAspectWrite aspectWrite : getPartType().getWriteAspects()) {
                    ValueNotifierHelpers.setValue(this, aspectErrorIds.get(aspectWrite), getPartState().getErrors(aspectWrite));
                }

                // Update state
                ValueNotifierHelpers.setValue(this, enabledId, partState.isEnabled());
                ValueNotifierHelpers.setValue(this, activeAspectId, partState.getActiveAspect() != null ? partState.getActiveAspect().getUniqueName().toString() : "");
            }
        } catch (PartStateException e) {
            player.closeScreen();
        }
    }

    public void setWriteValue(IFormattableTextComponent writeValue, int writeColor) {
        ValueNotifierHelpers.setValue(this, valueId, writeValue);
        ValueNotifierHelpers.setValue(this, colorId, writeColor);
    }

    public ITextComponent getWriteValue() {
        ITextComponent value = ValueNotifierHelpers.getValueTextComponent(this, valueId);
        if(value == null) {
            value = new StringTextComponent("");
        }
        return value;
    }

    public int getWriteValueColor() {
        return ValueNotifierHelpers.getValueInt(this, colorId);
    }

    public List<IFormattableTextComponent> getAspectErrors(IAspectWrite aspectWrite) {
        return ValueNotifierHelpers.getValueTextComponentList(this, aspectErrorIds.get(aspectWrite));
    }

    public boolean isPartStateEnabled() {
        return ValueNotifierHelpers.getValueBoolean(this, enabledId);
    }

    @Nullable
    public IAspect getPartStateActiveAspect() {
        return AspectRegistry.getInstance().getAspect(new ResourceLocation(ValueNotifierHelpers.getValueString(this, activeAspectId)));
    }

}
