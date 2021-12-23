package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.IntReferenceHolder;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.inventory.slot.FurnaceFuelSlotExtended;
import org.cyclops.integrateddynamics.tileentity.TileCoalGenerator;

/**
 * Container for the coal generator.
 * @author rubensworks
 */
public class ContainerCoalGenerator extends InventoryContainer {

    private final IntReferenceHolder referenceProgress;

    public ContainerCoalGenerator(int id, PlayerInventory playerInventory) {
        this(id, playerInventory, new Inventory(TileCoalGenerator.INVENTORY_SIZE), IntReferenceHolder.standalone());
    }

    public ContainerCoalGenerator(int id, PlayerInventory playerInventory, IInventory inventory, IntReferenceHolder progressReference) {
        super(RegistryEntries.CONTAINER_COAL_GENERATOR, id, playerInventory, inventory);

        this.referenceProgress = addDataSlot(progressReference);

        addInventory(inventory, 0, offsetX + 80, offsetY + 11, 1, 1);
        addPlayerInventory(playerInventory, offsetX + 8, offsetY + 46);
    }

    @Override
    public Slot createNewSlot(IInventory inventory, int index, int row, int column) {
        if(inventory instanceof PlayerInventory) {
            return super.createNewSlot(inventory, index, row, column);
        }
        return new FurnaceFuelSlotExtended(inventory, index, row, column);
    }

    public int getProgress() {
        return this.referenceProgress.get();
    }

    @Override
    public boolean stillValid(PlayerEntity p_75145_1_) {
        return false; // TODO: rm
    }
}
