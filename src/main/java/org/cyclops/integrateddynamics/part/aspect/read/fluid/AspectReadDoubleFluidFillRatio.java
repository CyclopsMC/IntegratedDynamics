package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraftforge.fluids.FluidTankInfo;

/**
 * Aspect that checks the target tank fill ratio.
 * @author rubensworks
 */
public class AspectReadDoubleFluidFillRatio extends AspectReadDoubleFluidActivatableBase {

    @Override
    protected String getUnlocalizedDoubleFluidType() {
        return "fillratio";
    }

    @Override
    protected double getValue(FluidTankInfo tankInfo) {
        double amount = tankInfo.fluid == null ? 0D : tankInfo.fluid.amount;
        return amount / (double) tankInfo.capacity;
    }
}
