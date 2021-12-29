package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
    private final InteractionHand hand;

    public ContainerLogicProgrammerPortable(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(id, playerInventory, ItemInventoryContainer.readItemIndex(packetBuffer),
                ItemInventoryContainer.readHand(packetBuffer));
    }

    public ContainerLogicProgrammerPortable(int id, Inventory playerInventory,
                                            int itemIndex, InteractionHand hand) {
        super(RegistryEntries.CONTAINER_LOGIC_PROGRAMMER_PORTABLE, id, playerInventory);
        this.itemIndex = itemIndex;
        this.hand = hand;
    }

    public ItemStack getItemStack(Player player) {
        return InventoryHelpers.getItemFromIndex(player, itemIndex, hand);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        ItemStack item = getItemStack(player);
        return item != null && item.getItem() == RegistryEntries.ITEM_PORTABLE_LOGIC_PROGRAMMER;
    }

}
