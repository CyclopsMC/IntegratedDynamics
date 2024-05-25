package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.slot.SlotRemoveOnly;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityDelay;
import org.cyclops.integrateddynamics.blockentity.BlockEntityProxy;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerActiveVariableBase;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;

import java.util.Optional;

/**
 * Container for the delay.
 * @author rubensworks
 */
public class ContainerDelay extends ContainerActiveVariableBase<BlockEntityDelay> {

    private final int lastUpdateValueId;
    private final int lastCapacityValueId;

    public ContainerDelay(int id, Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(BlockEntityDelay.INVENTORY_SIZE), Optional.empty());
    }

    public ContainerDelay(int id, Inventory playerInventory, Container inventory,
                          Optional<BlockEntityDelay> tileSupplier) {
        super(RegistryEntries.CONTAINER_DELAY.get(), id, playerInventory, inventory, tileSupplier);
        addSlot(new SlotVariable(inventory, BlockEntityProxy.SLOT_READ, 81, 25));
        addSlot(new SlotVariable(inventory, BlockEntityProxy.SLOT_WRITE_IN, 56, 78));
        addSlot(new SlotRemoveOnly(inventory, BlockEntityProxy.SLOT_WRITE_OUT, 104, 78));
        addPlayerInventory(playerInventory, offsetX + 9, offsetY + 145);

        lastUpdateValueId = getNextValueId();
        lastCapacityValueId = getNextValueId();
        getTileSupplier().ifPresent(tile -> tile.setLastPlayer(playerInventory.player));
    }

    @Override
    protected void initializeValues() {
        ValueNotifierHelpers.setValue(this, lastUpdateValueId, getTileSupplier().map(BlockEntityDelay::getUpdateInterval).orElse(0));
        ValueNotifierHelpers.setValue(this, lastCapacityValueId, getTileSupplier().map(BlockEntityDelay::getCapacity).orElse(0));
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
    public void onUpdate(int valueId, CompoundTag value) {
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
