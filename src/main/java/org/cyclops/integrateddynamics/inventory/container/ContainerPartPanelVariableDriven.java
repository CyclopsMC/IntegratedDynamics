package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipart;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.part.event.PartVariableDrivenVariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.part.panel.PartTypePanelVariableDriven;

import java.util.List;
import java.util.Optional;

/**
 * Container for display parts.
 * @author rubensworks
 */
public class ContainerPartPanelVariableDriven<P extends PartTypePanelVariableDriven<P, S>, S extends PartTypePanelVariableDriven.State<P, S>>
        extends ContainerMultipart<P, S> {

    private static final int SLOT_X = 79;
    private static final int SLOT_Y = 8;

    private final int readValueId;
    private final int readColorId;
    private final int readErrorsId;

    public ContainerPartPanelVariableDriven(int id, PlayerInventory playerInventory, PacketBuffer packetBuffer) {
        this(id, playerInventory, new Inventory(packetBuffer.readInt()),
                Optional.empty(), Optional.empty(), PartHelpers.readPart(packetBuffer));
    }

    public ContainerPartPanelVariableDriven(int id, PlayerInventory playerInventory, IInventory inventory,
                                            Optional<PartTarget> target, Optional<IPartContainer> partContainer, P partType) {
        super(RegistryEntries.CONTAINER_PART_DISPLAY, id, playerInventory, inventory, target, partContainer, partType);

        readValueId = getNextValueId();
        readColorId = getNextValueId();
        readErrorsId = getNextValueId();

        if (inventory instanceof SimpleInventory) {
            ((SimpleInventory) inventory).addDirtyMarkListener(this);
        }

        addInventory(inventory, 0, 80, 14, 1, 1);
        addPlayerInventory(player.inventory, 8, 46);
    }

    @Override
    protected Slot createNewSlot(IInventory inventory, int index, int x, int y) {
        if (inventory instanceof SimpleInventory) {
            return new SlotVariable(inventory, index, x, y);
        }
        return super.createNewSlot(inventory, index, x, y);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        if (!player.world.isRemote()) {
            IFormattableTextComponent readValue = new StringTextComponent("");
            int readValueColor = 0;
            if (!NetworkHelpers.shouldWork()) {
                readValue = new StringTextComponent("SAFE-MODE");
            } else {
                IValue value = getPartState().get().getDisplayValue();
                if (value != null) {
                    readValue = value.getType().toCompactString(value);
                    readValueColor = value.getType().getDisplayColor();
                }
            }
            ValueNotifierHelpers.setValue(this, readValueId, readValue);
            ValueNotifierHelpers.setValue(this, readColorId, readValueColor);
            ValueNotifierHelpers.setValue(this, readErrorsId, getPartState().get().getGlobalErrors());
        }
    }

    @Override
    public void onDirty() {
        if (!player.world.isRemote()) {
            S partState = getPartState().get();
            partState.onVariableContentsUpdated(getPartType(), getTarget().get());
            LazyOptional<INetwork> optionalNetwork = NetworkHelpers.getNetwork(getTarget().get().getCenter());
            if (!getContainerInventory().isEmpty()) {
                    NetworkHelpers.getPartNetwork(optionalNetwork).ifPresent(partNetwork -> {
                        try {
                            INetwork network = optionalNetwork.orElse(null);
                            IVariable variable = partState.getVariable(network, partNetwork);
                            MinecraftForge.EVENT_BUS.post(new PartVariableDrivenVariableContentsUpdatedEvent<>(network, partNetwork, getTarget().get(),
                                    getPartType(), partState, player, variable, variable != null ? variable.getValue() : null));
                        } catch (EvaluationException e) {

                        }
                    });

            }
            optionalNetwork
                    .ifPresent(network -> network.getEventBus().post(new VariableContentsUpdatedEvent(network)));
        }
    }

    @Override
    public void onContainerClosed(PlayerEntity player) {
        if (inventory instanceof SimpleInventory) {
            ((SimpleInventory) inventory).removeDirtyMarkListener(this);
        }
    }

    public ITextComponent getReadValue() {
        return ValueNotifierHelpers.getValueTextComponent(this, readValueId);
    }

    public int getReadValueColor() {
        return ValueNotifierHelpers.getValueInt(this, readColorId);
    }

    public List<IFormattableTextComponent> getReadErrors() {
        return ValueNotifierHelpers.getValueTextComponentList(this, readErrorsId);
    }
}
