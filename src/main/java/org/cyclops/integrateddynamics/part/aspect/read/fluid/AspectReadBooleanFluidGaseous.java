package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * Aspect that checks if the target tank is not empty.
 * @author rubensworks
 */
public class AspectReadBooleanFluidGaseous extends AspectReadBooleanFluidActivatableFluidBase {

    @Override
    protected String getUnlocalizedBooleanFluidType() {
        return "gaseous";
    }

    @Override
    protected boolean getValue(Fluid fluid, FluidStack fluidStack) {
        return fluid.isGaseous(fluidStack);
    }

}
