package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;
import org.cyclops.integrateddynamics.tileentity.TileVariablestore;

/**
 * Container for the variablestore.
 * @author rubensworks
 */
public class ContainerVariablestore extends InventoryContainer {

    public ContainerVariablestore(int id, PlayerInventory playerInventory) {
        this(id, playerInventory, new Inventory(TileVariablestore.INVENTORY_SIZE));
    }

    public ContainerVariablestore(int id, PlayerInventory playerInventory, IInventory inventory) {
        super(RegistryEntries.CONTAINER_VARIABLESTORE, id, playerInventory, inventory);
        addInventory(inventory, 0, offsetX + 8, offsetY + 18, TileVariablestore.ROWS, TileVariablestore.COLS);
        addPlayerInventory(playerInventory, offsetX + 8, offsetY + 14 + TileVariablestore.ROWS * 18 + 17);
    }

    @Override
    public Slot createNewSlot(IInventory inventory, int index, int row, int column) {
        if(inventory instanceof PlayerInventory) {
            return super.createNewSlot(inventory, index, row, column);
        }
        return new SlotVariable(inventory, index, row, column);
    }

    @Override
    public boolean stillValid(PlayerEntity p_75145_1_) {
        return false; // TODO: rm
    }

}
