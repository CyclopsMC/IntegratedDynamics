package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.inventory.slot.SlotRemoveOnly;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityProxy;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerActiveVariableBase;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;

import java.util.Optional;

/**
 * Container for the proxy.
 * @author rubensworks
 */
public class ContainerProxy extends ContainerActiveVariableBase<BlockEntityProxy> {

    public ContainerProxy(int id, Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(BlockEntityProxy.INVENTORY_SIZE), Optional.empty());
    }

    public ContainerProxy(int id, Inventory playerInventory, Container inventory,
                          Optional<BlockEntityProxy> tileSupplier) {
        super(RegistryEntries.CONTAINER_PROXY.get(), id, playerInventory, inventory, tileSupplier);
        addSlot(new SlotVariable(inventory, BlockEntityProxy.SLOT_READ, 81, 25));
        addSlot(new SlotVariable(inventory, BlockEntityProxy.SLOT_WRITE_IN, 56, 78));
        addSlot(new SlotRemoveOnly(inventory, BlockEntityProxy.SLOT_WRITE_OUT, 104, 78));
        addPlayerInventory(playerInventory, offsetX + 9, offsetY + 107);
        getTileSupplier().ifPresent(tile -> tile.setLastPlayer(playerInventory.player));
    }

}
