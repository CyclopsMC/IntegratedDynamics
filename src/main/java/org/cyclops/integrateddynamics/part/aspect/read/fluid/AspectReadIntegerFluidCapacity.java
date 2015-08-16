package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraftforge.fluids.FluidTankInfo;

/**
 * Aspect that checks the target tank capacity.
 * @author rubensworks
 */
public class AspectReadIntegerFluidCapacity extends AspectReadIntegerFluidActivatableBase {

    @Override
    protected String getUnlocalizedIntegerFluidType() {
        return "capacity";
    }

    @Override
    protected int getValue(FluidTankInfo tankInfo) {
        return tankInfo.capacity;
    }
}
