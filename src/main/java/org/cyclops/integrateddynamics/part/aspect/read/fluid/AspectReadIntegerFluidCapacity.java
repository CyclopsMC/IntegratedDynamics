package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraftforge.fluids.FluidTankInfo;

/**
 * Aspect that checks the target tank total capacity.
 * @author rubensworks
 */
public class AspectReadIntegerFluidCapacity extends AspectReadIntegerFluidBase {

    @Override
    protected String getUnlocalizedIntegerFluidType() {
        return "capacity";
    }

    @Override
    protected int getValue(FluidTankInfo[] tankInfo) {
        int capacity = 0;
        for(FluidTankInfo tank : tankInfo) {
            capacity += tank.capacity;
        }
        return capacity;
    }

}
