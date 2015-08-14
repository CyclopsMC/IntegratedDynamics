package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * Aspect that checks active tank fluid temperature.
 * @author rubensworks
 */
public class AspectReadIntegerFluidTemperature extends AspectReadIntegerFluidActivatableFluidBase {

    @Override
    protected String getUnlocalizedIntegerFluidType() {
        return "temperature";
    }

    @Override
    protected int getValue(Fluid fluid, FluidStack fluidStack) {
        return fluid.getTemperature(fluidStack);
    }

}
