package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.inventory.container.ItemInventoryContainer;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.item.ItemPortableLogicProgrammer;

/**
 * Container for the {@link ItemPortableLogicProgrammer}.
 * @author rubensworks
 */
public class ContainerLogicProgrammerPortable extends ContainerLogicProgrammerBase {

    private final int itemIndex;
    private final Hand hand;

    public ContainerLogicProgrammerPortable(int id, PlayerInventory playerInventory, PacketBuffer packetBuffer) {
        this(id, playerInventory, ItemInventoryContainer.readItemIndex(packetBuffer),
                ItemInventoryContainer.readHand(packetBuffer));
    }

    public ContainerLogicProgrammerPortable(int id, PlayerInventory playerInventory,
                                            int itemIndex, Hand hand) {
        super(RegistryEntries.CONTAINER_LOGIC_PROGRAMMER_PORTABLE, id, playerInventory);
        this.itemIndex = itemIndex;
        this.hand = hand;
    }

    public ItemStack getItemStack(PlayerEntity player) {
        return InventoryHelpers.getItemFromIndex(player, itemIndex, hand);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        ItemStack item = getItemStack(player);
        return item != null && item.getItem() == RegistryEntries.ITEM_PORTABLE_LOGIC_PROGRAMMER;
    }

}
