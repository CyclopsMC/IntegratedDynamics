package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import org.cyclops.cyclopscore.inventory.slot.SlotRemoveOnly;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMechanicalMachine;
import org.cyclops.integrateddynamics.tileentity.TileMechanicalSqueezer;

/**
 * Container for the mechanical squeezer.
 * @author rubensworks
 */
public class ContainerMechanicalSqueezer extends ContainerMechanicalMachine<TileMechanicalSqueezer> {

    public static final int BUTTON_TOGGLE_FLUID_EJECT = 0;

    /**
     * Make a new instance.
     * @param inventory The player inventory.
     * @param tile The part.
     */
    public ContainerMechanicalSqueezer(InventoryPlayer inventory, TileMechanicalSqueezer tile) {
        super(inventory, tile);

        addSlotToContainer(new Slot(tile, 0, 44, 37));

        addSlotToContainer(new SlotRemoveOnly(tile, 1, 98, 29));
        addSlotToContainer(new SlotRemoveOnly(tile, 2, 116, 29));
        addSlotToContainer(new SlotRemoveOnly(tile, 3, 98, 47));
        addSlotToContainer(new SlotRemoveOnly(tile, 4, 116, 47));

        addPlayerInventory(inventory, offsetX + 8, offsetY + 86);

        putButtonAction(BUTTON_TOGGLE_FLUID_EJECT,
                (buttonId, container) -> getTile().setAutoEjectFluids(!getTile().isAutoEjectFluids()));
    }

}
