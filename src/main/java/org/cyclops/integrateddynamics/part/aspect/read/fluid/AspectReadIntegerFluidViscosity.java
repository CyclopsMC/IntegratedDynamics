package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * Aspect that checks active tank fluid viscosity.
 * @author rubensworks
 */
public class AspectReadIntegerFluidViscosity extends AspectReadIntegerFluidActivatableFluidBase {

    @Override
    protected String getUnlocalizedIntegerFluidType() {
        return "viscosity";
    }

    @Override
    protected int getValue(Fluid fluid, FluidStack fluidStack) {
        return fluid.getViscosity(fluidStack);
    }

}
