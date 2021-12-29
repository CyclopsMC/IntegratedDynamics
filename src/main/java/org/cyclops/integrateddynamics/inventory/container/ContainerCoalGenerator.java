package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityCoalGenerator;
import org.cyclops.integrateddynamics.inventory.slot.FurnaceFuelSlotExtended;

/**
 * Container for the coal generator.
 * @author rubensworks
 */
public class ContainerCoalGenerator extends InventoryContainer {

    private final DataSlot referenceProgress;

    public ContainerCoalGenerator(int id, Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(BlockEntityCoalGenerator.INVENTORY_SIZE), DataSlot.standalone());
    }

    public ContainerCoalGenerator(int id, Inventory playerInventory, Container inventory, DataSlot progressReference) {
        super(RegistryEntries.CONTAINER_COAL_GENERATOR, id, playerInventory, inventory);

        this.referenceProgress = addDataSlot(progressReference);

        addInventory(inventory, 0, offsetX + 80, offsetY + 11, 1, 1);
        addPlayerInventory(playerInventory, offsetX + 8, offsetY + 46);
    }

    @Override
    public Slot createNewSlot(Container inventory, int index, int row, int column) {
        if(inventory instanceof Inventory) {
            return super.createNewSlot(inventory, index, row, column);
        }
        return new FurnaceFuelSlotExtended(inventory, index, row, column);
    }

    public int getProgress() {
        return this.referenceProgress.get();
    }
}
