package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * Aspect that checks active tank fluid name.
 * @author rubensworks
 */
public class AspectReadStringFluidName extends AspectReadStringFluidActivatableFluidBase {

    @Override
    protected String getUnlocalizedStringFluidType() {
        return "name";
    }

    @Override
    protected String getValue(Fluid fluid, FluidStack fluidStack) {
        return fluid.getLocalizedName(fluidStack);
    }

}
