package org.cyclops.integrateddynamics.inventory.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.FurnaceFuelSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FuelValues;

import java.util.function.Supplier;

/**
 * A {@link FurnaceFuelSlot} that does not put restrictions on the used tile entity.
 * @author rubensworks
 */
public class FurnaceFuelSlotExtended extends Slot {

    private final Supplier<FuelValues> fuelValuesSupplier;

    public FurnaceFuelSlotExtended(Container inventory, int index, int xPosition, int yPosition, Supplier<FuelValues> fuelValuesSupplier) {
        super(inventory, index, xPosition, yPosition);
        this.fuelValuesSupplier = fuelValuesSupplier;
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        return itemStack.getBurnTime(null, this.fuelValuesSupplier.get()) > 0 || FurnaceFuelSlot.isBucket(itemStack);
    }

    @Override
    public int getMaxStackSize(ItemStack itemStack) {
        return FurnaceFuelSlot.isBucket(itemStack) ? 1 : super.getMaxStackSize(itemStack);
    }

}
