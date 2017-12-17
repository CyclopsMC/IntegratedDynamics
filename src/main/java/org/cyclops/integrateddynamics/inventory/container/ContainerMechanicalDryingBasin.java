package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import org.cyclops.cyclopscore.inventory.slot.SlotRemoveOnly;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMechanicalMachine;
import org.cyclops.integrateddynamics.tileentity.TileMechanicalDryingBasin;

/**
 * Container for the mechanical drying basin.
 * @author rubensworks
 */
public class ContainerMechanicalDryingBasin extends ContainerMechanicalMachine<TileMechanicalDryingBasin> {

    /**
     * Make a new instance.
     * @param inventory The player inventory.
     * @param tile The part.
     */
    public ContainerMechanicalDryingBasin(InventoryPlayer inventory, TileMechanicalDryingBasin tile) {
        super(inventory, tile);

        addSlotToContainer(new Slot(tile, 0, 54, 37));

        addSlotToContainer(new SlotRemoveOnly(tile, 1, 108, 29));
        addSlotToContainer(new SlotRemoveOnly(tile, 2, 126, 29));
        addSlotToContainer(new SlotRemoveOnly(tile, 3, 108, 47));
        addSlotToContainer(new SlotRemoveOnly(tile, 4, 126, 47));

        addPlayerInventory(inventory, offsetX + 8, offsetY + 86);
    }

}
