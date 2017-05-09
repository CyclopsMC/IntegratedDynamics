package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.InventoryPlayer;
import org.cyclops.cyclopscore.inventory.slot.SlotRemoveOnly;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerActiveVariableBase;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;
import org.cyclops.integrateddynamics.tileentity.TileMaterializer;
import org.cyclops.integrateddynamics.tileentity.TileProxy;

/**
 * Container for the materializer.
 * @author rubensworks
 */
public class ContainerMaterializer extends ContainerActiveVariableBase<TileMaterializer> {

    /**
     * Make a new instance.
     * @param inventory The player inventory.
     * @param tile The part.
     */
    public ContainerMaterializer(InventoryPlayer inventory, TileMaterializer tile) {
        super(inventory, tile);
        addSlotToContainer(new SlotVariable(tile, TileProxy.SLOT_READ, 81, 25));
        addSlotToContainer(new SlotVariable(tile, TileProxy.SLOT_WRITE_IN, 56, 78));
        addSlotToContainer(new SlotRemoveOnly(tile, TileProxy.SLOT_WRITE_OUT, 104, 78));
        addPlayerInventory(inventory, offsetX + 9, offsetY + 107);
        tile.setLastPlayer(inventory.player);
    }

}
