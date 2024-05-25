package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.inventory.ItemLocation;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.item.ItemPortableLogicProgrammer;

/**
 * Container for the {@link ItemPortableLogicProgrammer}.
 * @author rubensworks
 */
public class ContainerLogicProgrammerPortable extends ContainerLogicProgrammerBase {

    private final ItemLocation itemLocation;

    public ContainerLogicProgrammerPortable(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(id, playerInventory, ItemLocation.readFromPacketBuffer(packetBuffer));
    }

    public ContainerLogicProgrammerPortable(int id, Inventory playerInventory, ItemLocation itemLocation) {
        super(RegistryEntries.CONTAINER_LOGIC_PROGRAMMER_PORTABLE.get(), id, playerInventory);
        this.itemLocation = itemLocation;
    }

    public ItemStack getItemStack(Player player) {
        return this.itemLocation.getItemStack(player);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        ItemStack item = getItemStack(player);
        return item != null && item.getItem() == RegistryEntries.ITEM_PORTABLE_LOGIC_PROGRAMMER.get();
    }

}
