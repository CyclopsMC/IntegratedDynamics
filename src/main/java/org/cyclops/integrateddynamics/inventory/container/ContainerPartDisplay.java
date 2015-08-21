package org.cyclops.integrateddynamics.inventory.container;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.player.EntityPlayer;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.core.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipart;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.tileentity.ITileCableNetwork;
import org.cyclops.integrateddynamics.part.PartTypeDisplay;

/**
 * Container for writer parts.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class ContainerPartDisplay extends ContainerMultipart<PartTypeDisplay, PartTypeDisplay.State> {

    private static final int SLOT_X = 79;
    private static final int SLOT_Y = 8;

    private final int valueId, colorId;

    /**
     * Make a new instance.
     * @param target        The target.
     * @param player        The player.
     * @param partContainer The part container.
     * @param partType      The part type.
     */
    public ContainerPartDisplay(EntityPlayer player, PartTarget target, IPartContainer partContainer, PartTypeDisplay partType) {
        super(player, target, partContainer, partType);

        SimpleInventory inventory = getPartState().getInventory();
        inventory.addDirtyMarkListener(this);

        addPlayerInventory(player.inventory, 8, 31);

        this.valueId = getNextValueId();
        this.colorId = getNextValueId();
    }

    @Override
    public void onDirty() {
        if(!MinecraftHelpers.isClientSide()) {
            getPartState().refresh(getPartType(), getTarget());
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
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        // TODO: abstract
        if(!MinecraftHelpers.isClientSide()) {
            String writeValue = "";
            int writeValueColor = 0;
            if(getPartContainer() instanceof ITileCableNetwork && getPartState().getGlobalErrors().isEmpty()) {
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
            setWriteValue(writeValue, writeValueColor);
        }
    }

    public void setWriteValue(String writeValue, int writeColor) {
        ValueNotifierHelpers.setValue(this, valueId, writeValue);
        ValueNotifierHelpers.setValue(this, colorId, writeColor);
    }

    public String getWriteValue() {
        String value = ValueNotifierHelpers.getValueString(this, valueId);
        if(value == null) {
            value = "";
        }
        return value;
    }

    public int getWriteValueColor() {
        return ValueNotifierHelpers.getValueInt(this, colorId);
    }

    @Override
    protected int getSizeInventory() {
        return getPartState().getInventory().getSizeInventory();
    }
}
