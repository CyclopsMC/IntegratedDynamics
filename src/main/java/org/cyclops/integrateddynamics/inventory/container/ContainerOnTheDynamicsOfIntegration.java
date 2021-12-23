package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import org.cyclops.cyclopscore.inventory.container.ItemInventoryContainer;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Container for the On the Dynamics of Integration book.
 * @author rubensworks
 */
public class ContainerOnTheDynamicsOfIntegration extends ItemInventoryContainer {

    public ContainerOnTheDynamicsOfIntegration(int id, PlayerInventory inventory, PacketBuffer packetBuffer) {
        this(id, inventory, readItemIndex(packetBuffer), readHand(packetBuffer));
    }

    public ContainerOnTheDynamicsOfIntegration(int id, PlayerInventory playerInventory, int itemIndex, Hand hand) {
        super(RegistryEntries.CONTAINER_ON_THE_DYNAMICS_OF_INTEGRATION, id, playerInventory, itemIndex, hand);
    }

    @Override
    protected int getSizeInventory() {
        return 0;
    }

    @Override
    public boolean stillValid(PlayerEntity p_75145_1_) {
        return false; // TODO: rm
    }
}
