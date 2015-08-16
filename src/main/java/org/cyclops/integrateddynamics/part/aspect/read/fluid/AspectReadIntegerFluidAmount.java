package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * Aspect that checks the target tank amount.
 * @author rubensworks
 */
public class AspectReadIntegerFluidAmount extends AspectReadIntegerFluidActivatableFluidBase {

    @Override
    protected String getUnlocalizedIntegerFluidType() {
        return "amount";
    }

    @Override
    protected int getValue(Fluid fluid, FluidStack fluidStack) {
        return fluidStack.amount;
    }
}
