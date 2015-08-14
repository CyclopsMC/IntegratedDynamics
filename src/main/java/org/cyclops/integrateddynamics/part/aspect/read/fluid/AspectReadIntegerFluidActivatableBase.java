package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraftforge.fluids.FluidTankInfo;

/**
 * Abstract aspect that has an activatable tank
 * @author rubensworks
 */
public abstract class AspectReadIntegerFluidActivatableBase extends AspectReadIntegerFluidBase {

    @Override
    protected int getValue(FluidTankInfo[] tankInfo) {
        int i = getActiveTank();
        if(i < tankInfo.length) {
            return getValue(tankInfo[i]);
        }
        return getDefaultValue();
    }

    protected int getActiveTank() {
        return 0; // TODO: with aspect props
    }

    protected abstract int getValue(FluidTankInfo tankInfo);
}
