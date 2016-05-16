package org.cyclops.integrateddynamics.modcompat.charset.aspect;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import pl.asie.charset.api.pipes.IShifter;

/**
 * Dynamic Shifter implementation.
 * @author rubensworks
 */
public class ShifterPart implements IShifter {

    private final EnumFacing direction;
    private boolean shifting;
    private Iterable<ValueObjectTypeItemStack.ValueItemStack> filterItem;
    private Iterable<ValueObjectTypeFluidStack.ValueFluidStack> filterFluid;

    public ShifterPart(EnumFacing direction) {
        this.direction = direction;
        this.shifting = false;
        this.filterItem = null;
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
        return filterItem != null || filterFluid != null;
    }

    public void setFilterItem(Iterable<ValueObjectTypeItemStack.ValueItemStack> filterItem) {
        this.filterItem = filterItem;
    }

    public void setFilterFluid(Iterable<ValueObjectTypeFluidStack.ValueFluidStack> filterFluid) {
        this.filterFluid = filterFluid;
    }

    @Override
    public boolean matches(ItemStack source) {
        if(filterItem != null) {
            for(ValueObjectTypeItemStack.ValueItemStack itemStack : filterItem) {
                if(itemStack.getRawValue().isPresent()
                        && ItemStackHelpers.areItemStacksIdentical(itemStack.getRawValue().get(), source)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(FluidStack source) {
        if(filterFluid != null) {
            for(ValueObjectTypeFluidStack.ValueFluidStack fluidStack : filterFluid) {
                if (fluidStack.getRawValue().isPresent()) {
                    FluidStack self = fluidStack.getRawValue().get();
                    if ((self == null && source == null)
                            || (self != null && source != null && self.getFluid() == source.getFluid())) {
                        return true;
                    }
                }
            }
            return false;
        }
        return true;
    }
}
