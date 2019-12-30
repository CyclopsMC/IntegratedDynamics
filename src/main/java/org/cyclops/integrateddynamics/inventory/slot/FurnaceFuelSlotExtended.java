package org.cyclops.integrateddynamics.inventory.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.FurnaceFuelSlot;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;

/**
 * A {@link FurnaceFuelSlot} that does not put restrictions on the used tile entity.
 * @author rubensworks
 */
public class FurnaceFuelSlotExtended extends Slot {

    public FurnaceFuelSlotExtended(IInventory inventory, int index, int xPosition, int yPosition) {
        super(inventory, index, xPosition, yPosition);
    }

    public boolean isItemValid(ItemStack itemStack) {
        return AbstractFurnaceTileEntity.isFuel(itemStack) || FurnaceFuelSlot.isBucket(itemStack);
    }

    public int getItemStackLimit(ItemStack itemStack) {
        return FurnaceFuelSlot.isBucket(itemStack) ? 1 : super.getItemStackLimit(itemStack);
    }

}
