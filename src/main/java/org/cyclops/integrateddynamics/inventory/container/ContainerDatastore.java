package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import org.cyclops.cyclopscore.inventory.container.TileInventoryContainer;
import org.cyclops.cyclopscore.inventory.slot.SlotSingleItem;
import org.cyclops.integrateddynamics.item.ItemVariable;
import org.cyclops.integrateddynamics.tileentity.TileDatastore;

/**
 * Container for the datastore.
 * @author rubensworks
 */
public class ContainerDatastore extends TileInventoryContainer<TileDatastore> {

    /**
     * Make a new instance.
     * @param inventory The player inventory.
     * @param tile The tile.
     */
    public ContainerDatastore(InventoryPlayer inventory, TileDatastore tile) {
        super(inventory, tile);
        addInventory(tile, 0, offsetX + 8, offsetY + 18, TileDatastore.ROWS, TileDatastore.COLS);
        addPlayerInventory(inventory, offsetX + 8, offsetY + 14 + TileDatastore.ROWS * 18 + 17);
    }

    @Override
    public Slot createNewSlot(IInventory inventory, int index, int row, int column) {
        if(inventory instanceof InventoryPlayer) {
            return super.createNewSlot(inventory, index, row, column);
        }
        return new SlotSingleItem(inventory, index, row, column, ItemVariable.getInstance());
    }

}
