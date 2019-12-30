package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.CompoundNBT;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.slot.SlotRemoveOnly;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerActiveVariableBase;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;
import org.cyclops.integrateddynamics.tileentity.TileDelay;
import org.cyclops.integrateddynamics.tileentity.TileProxy;

import java.util.Optional;

/**
 * Container for the delay.
 * @author rubensworks
 */
public class ContainerDelay extends ContainerActiveVariableBase<TileDelay> {

    private final int lastUpdateValueId;
    private final int lastCapacityValueId;

    public ContainerDelay(int id, PlayerInventory playerInventory) {
        this(id, playerInventory, new Inventory(TileDelay.INVENTORY_SIZE), Optional.empty());
    }

    public ContainerDelay(int id, PlayerInventory playerInventory, IInventory inventory,
                          Optional<TileDelay> tileSupplier) {
        super(RegistryEntries.CONTAINER_DELAY, id, playerInventory, inventory, tileSupplier);
        addSlot(new SlotVariable(inventory, TileProxy.SLOT_READ, 81, 25));
        addSlot(new SlotVariable(inventory, TileProxy.SLOT_WRITE_IN, 56, 78));
        addSlot(new SlotRemoveOnly(inventory, TileProxy.SLOT_WRITE_OUT, 104, 78));
        addPlayerInventory(playerInventory, offsetX + 9, offsetY + 145);

        lastUpdateValueId = getNextValueId();
        lastCapacityValueId = getNextValueId();
        getTileSupplier().ifPresent(tile -> tile.setLastPlayer(playerInventory.player));
    }

    @Override
    protected void initializeValues() {
        ValueNotifierHelpers.setValue(this, lastUpdateValueId, getTileSupplier().map(TileDelay::getUpdateInterval).orElse(0));
        ValueNotifierHelpers.setValue(this, lastCapacityValueId, getTileSupplier().map(TileDelay::getCapacity).orElse(0));
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
    public void onUpdate(int valueId, CompoundNBT value) {
        super.onUpdate(valueId, value);
        getTileSupplier().ifPresent(tile -> {
            if (valueId == getLastUpdateValueId()) {
                tile.setUpdateInterval(getLastUpdateValue());
            } else if (valueId == getLastCapacityValueId()) {
                tile.setCapacity(getLastCapacityValue());
            }
        });
    }
}
