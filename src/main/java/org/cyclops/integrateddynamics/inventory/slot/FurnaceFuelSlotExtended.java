package org.cyclops.integrateddynamics.inventory.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.FurnaceFuelSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

/**
 * A {@link FurnaceFuelSlot} that does not put restrictions on the used tile entity.
 * @author rubensworks
 */
public class FurnaceFuelSlotExtended extends Slot {

    public FurnaceFuelSlotExtended(Container inventory, int index, int xPosition, int yPosition) {
        super(inventory, index, xPosition, yPosition);
    }

    public boolean mayPlace(ItemStack itemStack) {
        return AbstractFurnaceBlockEntity.isFuel(itemStack) || FurnaceFuelSlot.isBucket(itemStack);
    }

    public int getMaxStackSize(ItemStack itemStack) {
        return FurnaceFuelSlot.isBucket(itemStack) ? 1 : super.getMaxStackSize(itemStack);
    }

}
