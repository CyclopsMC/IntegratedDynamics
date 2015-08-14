package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

/**
 * Base aspect to check a fluid.
 * @author rubensworks
 */
public abstract class AspectReadIntegerFluidActivatableFluidBase extends AspectReadIntegerFluidActivatableBase {

    protected abstract int getValue(Fluid fluid, FluidStack fluidStack);

    @Override
    protected int getValue(FluidTankInfo tankInfo) {
        int value = 0;
        FluidStack fluidStack = tankInfo.fluid;
        if(fluidStack != null) {
            value = getValue(fluidStack.getFluid(), fluidStack);
        }
        return value;
    }

}
