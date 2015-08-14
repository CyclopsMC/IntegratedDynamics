package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraftforge.fluids.FluidTankInfo;

/**
 * Aspect that checks if the target tank is full.
 * @author rubensworks
 */
public class AspectReadBooleanFluidFull extends AspectReadBooleanFluidBase {

    @Override
    protected String getUnlocalizedBooleanFluidType() {
        return "full";
    }

    @Override
    protected boolean getValue(FluidTankInfo[] tankInfo) {
        boolean allFull = true;
        for(FluidTankInfo tank : tankInfo) {
            if(tank.fluid == null && tank.capacity > 0 || (tank.fluid != null && tank.fluid.amount < tank.capacity)) {
                allFull = false;
            }
        }
        return allFull;
    }

}
