package org.cyclops.integrateddynamics.modcompat.charset.aspect;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import pl.asie.charset.api.pipes.IShifter;

/**
 * Dynamic Shifter implementation.
 * @author rubensworks
 */
public class ShifterPart implements IShifter {

    private final EnumFacing direction;
    private boolean shifting;
    private Iterable<ValueObjectTypeItemStack.ValueItemStack> filter;

    public ShifterPart(EnumFacing direction) {
        this.direction = direction;
        this.shifting = false;
        this.filter = null;
    }

    @Override
    public Mode getMode() {
        return Mode.Shift;
    }

    @Override
    public EnumFacing getDirection() {
        return direction;
    }

    @Override
    public int getShiftDistance() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isShifting() {
        return shifting;
    }

    public void setShifting(boolean shifting) {
        this.shifting = shifting;
    }

    @Override
    public boolean hasFilter() {
        return filter != null;
    }

    public void setFilter(Iterable<ValueObjectTypeItemStack.ValueItemStack> filter) {
        this.filter = filter;
    }

    @Override
    public boolean matches(ItemStack source) {
        if(filter != null) {
            for(ValueObjectTypeItemStack.ValueItemStack itemStack : filter) {
                if(itemStack.getRawValue().isPresent()
                        && ItemStackHelpers.areItemStacksIdentical(itemStack.getRawValue().get(), source)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
}
