package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.inventory.container.ItemInventoryContainer;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Container for the On the Dynamics of Integration book.
 * @author rubensworks
 */
public class ContainerOnTheDynamicsOfIntegration extends ItemInventoryContainer {

    public ContainerOnTheDynamicsOfIntegration(int id, Inventory inventory, FriendlyByteBuf packetBuffer) {
        this(id, inventory, readItemIndex(packetBuffer), readHand(packetBuffer));
    }

    public ContainerOnTheDynamicsOfIntegration(int id, Inventory playerInventory, int itemIndex, InteractionHand hand) {
        super(RegistryEntries.CONTAINER_ON_THE_DYNAMICS_OF_INTEGRATION, id, playerInventory, itemIndex, hand);
    }

    @Override
    protected int getSizeInventory() {
        return 0;
    }
}
