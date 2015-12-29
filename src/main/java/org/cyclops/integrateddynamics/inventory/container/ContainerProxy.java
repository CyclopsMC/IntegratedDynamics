package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.InventoryPlayer;
import org.cyclops.cyclopscore.inventory.container.TileInventoryContainerConfigurable;
import org.cyclops.cyclopscore.inventory.slot.SlotRemoveOnly;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;
import org.cyclops.integrateddynamics.tileentity.TileProxy;

/**
 * Container for the proxy.
 * @author rubensworks
 */
public class ContainerProxy extends TileInventoryContainerConfigurable<TileProxy> {

    /**
     * Make a new instance.
     * @param inventory The player inventory.
     * @param tile The tile.
     */
    public ContainerProxy(InventoryPlayer inventory, TileProxy tile) {
        super(inventory, tile);
        addSlotToContainer(new SlotVariable(tile, 0, 81, 25));
        addSlotToContainer(new SlotVariable(tile, 1, 56, 78));
        addSlotToContainer(new SlotRemoveOnly(tile, 2, 104, 78));
        addPlayerInventory(inventory, offsetX + 9, offsetY + 107);
    }

}
