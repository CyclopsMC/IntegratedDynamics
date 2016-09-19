package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.integrateddynamics.item.ItemPortableLogicProgrammer;

/**
 * Container for the {@link ItemPortableLogicProgrammer}.
 * @author rubensworks
 */
public class ContainerLogicProgrammerPortable extends ContainerLogicProgrammerBase {

    private final int itemIndex;

    public ContainerLogicProgrammerPortable(EntityPlayer player, int itemIndex) {
        super(player.inventory, ItemPortableLogicProgrammer.getInstance());
        this.itemIndex = itemIndex;
    }

    public ItemStack getItemStack(EntityPlayer player) {
        return InventoryHelpers.getItemFromIndex(player, itemIndex);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        ItemStack item = getItemStack(player);
        return item != null && item.getItem() == ItemPortableLogicProgrammer.getInstance();
    }

}
