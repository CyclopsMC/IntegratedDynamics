package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * Aspect that checks active tank fluid luminosity.
 * @author rubensworks
 */
public class AspectReadIntegerFluidLuminosity extends AspectReadIntegerFluidActivatableFluidBase {

    @Override
    protected String getUnlocalizedIntegerFluidType() {
        return "luminosity";
    }

    @Override
    protected int getValue(Fluid fluid, FluidStack fluidStack) {
        return fluid.getLuminosity(fluidStack);
    }

}
