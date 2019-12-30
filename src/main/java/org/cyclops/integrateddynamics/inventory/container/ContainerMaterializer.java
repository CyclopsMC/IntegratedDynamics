package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import org.cyclops.cyclopscore.inventory.slot.SlotRemoveOnly;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerActiveVariableBase;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;
import org.cyclops.integrateddynamics.tileentity.TileMaterializer;
import org.cyclops.integrateddynamics.tileentity.TileProxy;

import java.util.Optional;

/**
 * Container for the materializer.
 * @author rubensworks
 */
public class ContainerMaterializer extends ContainerActiveVariableBase<TileMaterializer> {

    public ContainerMaterializer(int id, PlayerInventory playerInventory) {
        this(id, playerInventory, new Inventory(TileMaterializer.INVENTORY_SIZE), Optional.empty());
    }

    public ContainerMaterializer(int id, PlayerInventory playerInventory, IInventory inventory,
                          Optional<TileMaterializer> tileSupplier) {
        super(RegistryEntries.CONTAINER_MATERIALIZER, id, playerInventory, inventory, tileSupplier);
        addSlot(new SlotVariable(inventory, TileProxy.SLOT_READ, 81, 25));
        addSlot(new SlotVariable(inventory, TileProxy.SLOT_WRITE_IN, 56, 78));
        addSlot(new SlotRemoveOnly(inventory, TileProxy.SLOT_WRITE_OUT, 104, 78));
        addPlayerInventory(playerInventory, offsetX + 9, offsetY + 107);
        getTileSupplier().ifPresent(tile -> tile.setLastPlayer(playerInventory.player));
    }

}
