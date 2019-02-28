package org.cyclops.integrateddynamics.inventory.container;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraftforge.common.MinecraftForge;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipart;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.part.event.PartVariableDrivenVariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.part.panel.PartTypePanelVariableDriven;

/**
 * Container for display parts.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class ContainerPartDisplay<P extends PartTypePanelVariableDriven<P, S>, S extends PartTypePanelVariableDriven.State<P, S>> extends ContainerMultipart<P, S> {

    private static final int SLOT_X = 79;
    private static final int SLOT_Y = 8;

    private final int readValueId;
    private final int readColorId;

    /**
     * Make a new instance.
     * @param target        The target.
     * @param player        The player.
     * @param partContainer The part container.
     * @param partType      The part type.
     */
    public ContainerPartDisplay(EntityPlayer player, PartTarget target, IPartContainer partContainer, IPartType partType) {
        super(player, target, partContainer, (P) partType);

        readValueId = getNextValueId();
        readColorId = getNextValueId();

        SimpleInventory inventory = getPartState().getInventory();
        inventory.addDirtyMarkListener(this);

        addInventory(getPartState().getInventory(), 0, 80, 14, 1, 1);
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

        if(!MinecraftHelpers.isClientSide()) {
            String readValue = "";
            int readValueColor = 0;
            if (!NetworkHelpers.shouldWork()) {
                readValue = "SAFE-MODE";
            } else {
                IValue value = getPartState().getDisplayValue();
                if (value != null) {
                    readValue = value.getType().toCompactString(value);
                    readValueColor = value.getType().getDisplayColor();
                }
            }
            ValueNotifierHelpers.setValue(this, readValueId, readValue);
            ValueNotifierHelpers.setValue(this, readColorId, readValueColor);
        }
    }

    @Override
    public void onDirty() {
        if(!MinecraftHelpers.isClientSide()) {
            getPartState().onVariableContentsUpdated(getPartType(), getTarget());
            INetwork network = NetworkHelpers.getNetwork(getTarget().getCenter());
            if (!getPartState().getInventory().isEmpty()) {
                try {
                    IPartNetwork partNetwork = NetworkHelpers.getPartNetwork(network);
                    IVariable variable = getPartState().getVariable(getTarget(), partNetwork);
                    MinecraftForge.EVENT_BUS.post(new PartVariableDrivenVariableContentsUpdatedEvent<>(network, partNetwork, getTarget(),
                            getPartType(), getPartState(), getPlayer(), variable, variable != null ? variable.getValue() : null));
                } catch (EvaluationException e) {

                }
            }
            if (network != null) {
                network.getEventBus().post(new VariableContentsUpdatedEvent(network));
            }
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        getPartState().getInventory().removeDirtyMarkListener(this);
    }

    @Override
    protected int getSizeInventory() {
        return getPartState().getInventory().getSizeInventory();
    }

    public String getReadValue() {
        return ValueNotifierHelpers.getValueString(this, readValueId);
    }

    public int getReadValueColor() {
        return ValueNotifierHelpers.getValueInt(this, readColorId);
    }
}
