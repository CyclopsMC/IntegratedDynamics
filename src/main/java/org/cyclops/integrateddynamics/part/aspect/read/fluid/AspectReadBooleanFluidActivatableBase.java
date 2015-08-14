package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraftforge.fluids.FluidTankInfo;

/**
 * Abstract aspect that has an activatable tank
 * @author rubensworks
 */
public abstract class AspectReadBooleanFluidActivatableBase extends AspectReadBooleanFluidBase {

    @Override
    protected boolean getValue(FluidTankInfo[] tankInfo) {
        int i = getActiveTank();
        if(i < tankInfo.length) {
            return getValue(tankInfo[i]);
        }
        return false;
    }

    protected int getActiveTank() {
        return 0; // TODO: with aspect props
    }

    protected abstract boolean getValue(FluidTankInfo tankInfo);
}
