package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.slot.SlotRemoveOnly;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerActiveVariableBase;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;
import org.cyclops.integrateddynamics.tileentity.TileDelay;
import org.cyclops.integrateddynamics.tileentity.TileProxy;

/**
 * Container for the delay.
 * @author rubensworks
 */
public class ContainerDelay extends ContainerActiveVariableBase<TileDelay> {

    private final int lastUpdateValueId;
    private final int lastCapacityValueId;

    /**
     * Make a new instance.
     * @param inventory The player inventory.
     * @param tile The part.
     */
    public ContainerDelay(InventoryPlayer inventory, TileDelay tile) {
        super(inventory, tile);
        addSlotToContainer(new SlotVariable(tile, TileProxy.SLOT_READ, 81, 25));
        addSlotToContainer(new SlotVariable(tile, TileProxy.SLOT_WRITE_IN, 56, 78));
        addSlotToContainer(new SlotRemoveOnly(tile, TileProxy.SLOT_WRITE_OUT, 104, 78));
        addPlayerInventory(inventory, offsetX + 9, offsetY + 145);

        lastUpdateValueId = getNextValueId();
        lastCapacityValueId = getNextValueId();
    }

    @Override
    protected void initializeValues() {
        ValueNotifierHelpers.setValue(this, lastUpdateValueId, getTile().getUpdateInterval());
        ValueNotifierHelpers.setValue(this, lastCapacityValueId, getTile().getCapacity());
    }

    public int getLastUpdateValueId() {
        return lastUpdateValueId;
    }

    public int getLastCapacityValueId() {
        return lastCapacityValueId;
    }

    public int getLastUpdateValue() {
        return ValueNotifierHelpers.getValueInt(this, getLastUpdateValueId());
    }

    public int getLastCapacityValue() {
        return ValueNotifierHelpers.getValueInt(this, getLastCapacityValueId());
    }

    @Override
    public void onUpdate(int valueId, NBTTagCompound value) {
        super.onUpdate(valueId, value);
        if(!getTile().getWorld().isRemote) {
            if (valueId == getLastUpdateValueId()) {
                getTile().setUpdateInterval(getLastUpdateValue());
            } else if (valueId == getLastCapacityValueId()) {
                getTile().setCapacity(getLastCapacityValue());
            }
        }
    }
}
