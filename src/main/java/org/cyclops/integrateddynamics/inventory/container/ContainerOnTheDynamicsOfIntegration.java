package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.inventory.ItemLocation;
import org.cyclops.cyclopscore.inventory.container.ItemInventoryContainer;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Container for the On the Dynamics of Integration book.
 * @author rubensworks
 */
public class ContainerOnTheDynamicsOfIntegration extends ItemInventoryContainer {

    public ContainerOnTheDynamicsOfIntegration(int id, Inventory inventory, FriendlyByteBuf packetBuffer) {
        this(id, inventory, ItemLocation.readFromPacketBuffer(packetBuffer));
    }

    public ContainerOnTheDynamicsOfIntegration(int id, Inventory playerInventory, ItemLocation itemLocation) {
        super(RegistryEntries.CONTAINER_ON_THE_DYNAMICS_OF_INTEGRATION, id, playerInventory, itemLocation);
    }

    @Override
    protected int getSizeInventory() {
        return 0;
    }
}
