package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * Aspect that checks active tank fluid density.
 * @author rubensworks
 */
public class AspectReadIntegerFluidDensity extends AspectReadIntegerFluidActivatableFluidBase {

    @Override
    protected String getUnlocalizedIntegerFluidType() {
        return "density";
    }

    @Override
    protected int getValue(Fluid fluid, FluidStack fluidStack) {
        return fluid.getDensity(fluidStack);
    }

}
