package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.inventory.slot.SlotRemoveOnly;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityMaterializer;
import org.cyclops.integrateddynamics.blockentity.BlockEntityProxy;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerActiveVariableBase;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;

import java.util.Optional;

/**
 * Container for the materializer.
 * @author rubensworks
 */
public class ContainerMaterializer extends ContainerActiveVariableBase<BlockEntityMaterializer> {

    public ContainerMaterializer(int id, Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(BlockEntityMaterializer.INVENTORY_SIZE), Optional.empty());
    }

    public ContainerMaterializer(int id, Inventory playerInventory, Container inventory,
                          Optional<BlockEntityMaterializer> tileSupplier) {
        super(RegistryEntries.CONTAINER_MATERIALIZER.get(), id, playerInventory, inventory, tileSupplier);
        addSlot(new SlotVariable(inventory, BlockEntityProxy.SLOT_READ, 81, 25));
        addSlot(new SlotVariable(inventory, BlockEntityProxy.SLOT_WRITE_IN, 56, 78));
        addSlot(new SlotRemoveOnly(inventory, BlockEntityProxy.SLOT_WRITE_OUT, 104, 78));
        addPlayerInventory(playerInventory, offsetX + 9, offsetY + 107);
        getTileSupplier().ifPresent(tile -> tile.setLastPlayer(playerInventory.player));
    }

}
