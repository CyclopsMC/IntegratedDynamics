package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.cyclops.cyclopscore.inventory.ItemLocation;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.inventory.container.ItemInventoryContainer;
import org.cyclops.cyclopscore.inventory.slot.SlotExtended;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.helper.Helpers;
import org.cyclops.integrateddynamics.item.ItemLabeller;

/**
 * Container for the labeller.
 * @author rubensworks
 */
public class ContainerLabeller extends ItemInventoryContainer<ItemLabeller> {

    private SimpleInventory temporaryInputSlots = null;

    public ContainerLabeller(int id, Inventory inventory, FriendlyByteBuf packetBuffer) {
        this(id, inventory, ItemLocation.readFromPacketBuffer(packetBuffer));
    }

    public ContainerLabeller(int id, Inventory inventory, ItemLocation itemLocation) {
        super(RegistryEntries.CONTAINER_LABELLER.get(), id, inventory, itemLocation);
        this.temporaryInputSlots = new SimpleInventory(1, 1);
        addSlot(new SlotExtended(temporaryInputSlots, 0, 8, 8));
        this.addPlayerInventory(player.getInventory(), 8, 31);
    }

    public SimpleInventory getTemporaryInputSlots() {
        return temporaryInputSlots;
    }

    public ItemStack getItemStack() {
        return temporaryInputSlots.getItem(0);
    }

    @Override
    protected int getSizeInventory() {
        return 1;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            ItemStack itemStack = temporaryInputSlots.getItem(0);
            if(!itemStack.isEmpty()) {
                Helpers.returnItemToPlayer(player, itemStack);
            }
        }
    }

    public void setItemStackName(String name) {
        ItemStack itemStack = getItemStack();
        if(!itemStack.isEmpty()) {
            if (StringUtils.isBlank(name)) {
                itemStack.remove(DataComponents.CUSTOM_NAME);
            } else {
                itemStack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
            }
        }
    }
}
