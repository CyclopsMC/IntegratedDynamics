package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

/**
 * Base aspect to check a fluid.
 * @author rubensworks
 */
public abstract class AspectReadStringFluidActivatableFluidBase extends AspectReadStringFluidActivatableBase {

    protected abstract String getValue(Fluid fluid, FluidStack fluidStack);

    @Override
    protected String getValue(FluidTankInfo tankInfo) {
        String value = getDefaultValue();
        FluidStack fluidStack = tankInfo.fluid;
        if(fluidStack != null) {
            value = getValue(fluidStack.getFluid(), fluidStack);
        }
        return value;
    }

}
