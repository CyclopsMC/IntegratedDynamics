package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.InventoryPlayer;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.container.TileInventoryContainerConfigurable;
import org.cyclops.cyclopscore.inventory.slot.SlotRemoveOnly;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;
import org.cyclops.integrateddynamics.tileentity.TileProxy;

/**
 * Container for the proxy.
 * @author rubensworks
 */
public class ContainerProxy extends TileInventoryContainerConfigurable<TileProxy> {

    private final int readValueId;
    private final int readColorId;

    /**
     * Make a new instance.
     * @param inventory The player inventory.
     * @param tile The tile.
     */
    public ContainerProxy(InventoryPlayer inventory, TileProxy tile) {
        super(inventory, tile);
        addSlotToContainer(new SlotVariable(tile, TileProxy.SLOT_READ, 81, 25));
        addSlotToContainer(new SlotVariable(tile, TileProxy.SLOT_WRITE_IN, 56, 78));
        addSlotToContainer(new SlotRemoveOnly(tile, TileProxy.SLOT_WRITE_OUT, 104, 78));
        addPlayerInventory(inventory, offsetX + 9, offsetY + 107);

        readValueId = getNextValueId();
        readColorId = getNextValueId();
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        if(!MinecraftHelpers.isClientSide()) {
            String readValue = "";
            int readValueColor = 0;
            IVariable variable = getTile().getVariable(getTile().getNetwork());
            if(variable != null) {
                try {
                    IValue value = variable.getValue();
                    readValue = value.getType().toCompactString(value);
                    readValueColor = variable.getType().getDisplayColor();
                } catch (EvaluationException | NullPointerException e) {
                    readValue = "ERROR";
                    readValueColor = Helpers.RGBToInt(255, 0, 0);
                }
            }
            ValueNotifierHelpers.setValue(this, readValueId, readValue);
            ValueNotifierHelpers.setValue(this, readColorId, readValueColor);
        }
    }

    public String getReadValue() {
        return ValueNotifierHelpers.getValueString(this, readValueId);
    }

    public int getReadValueColor() {
        return ValueNotifierHelpers.getValueInt(this, readColorId);
    }

}
