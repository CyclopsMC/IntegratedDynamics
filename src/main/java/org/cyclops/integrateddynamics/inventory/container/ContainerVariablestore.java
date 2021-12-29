package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityVariablestore;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;

/**
 * Container for the variablestore.
 * @author rubensworks
 */
public class ContainerVariablestore extends InventoryContainer {

    public ContainerVariablestore(int id, Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(BlockEntityVariablestore.INVENTORY_SIZE));
    }

    public ContainerVariablestore(int id, Inventory playerInventory, Container inventory) {
        super(RegistryEntries.CONTAINER_VARIABLESTORE, id, playerInventory, inventory);
        addInventory(inventory, 0, offsetX + 8, offsetY + 18, BlockEntityVariablestore.ROWS, BlockEntityVariablestore.COLS);
        addPlayerInventory(playerInventory, offsetX + 8, offsetY + 14 + BlockEntityVariablestore.ROWS * 18 + 17);
    }

    @Override
    public Slot createNewSlot(Container inventory, int index, int row, int column) {
        if(inventory instanceof Inventory) {
            return super.createNewSlot(inventory, index, row, column);
        }
        return new SlotVariable(inventory, index, row, column);
    }
}
