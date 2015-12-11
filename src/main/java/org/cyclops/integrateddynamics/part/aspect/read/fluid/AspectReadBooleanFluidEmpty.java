package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraftforge.fluids.FluidTankInfo;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;

/**
 * Aspect that checks if the target tank is not empty.
 * @author rubensworks
 */
public class AspectReadBooleanFluidEmpty extends AspectReadBooleanFluidBase {

    @Override
    protected String getUnlocalizedBooleanFluidType() {
        return "empty";
    }

    @Override
    protected boolean getValue(FluidTankInfo[] tankInfo, IAspectProperties properties) {
        for(FluidTankInfo tank : tankInfo) {
            if(tank.fluid != null && tank.capacity > 0 || (tank.fluid != null && tank.fluid.amount < tank.capacity)) {
                return false;
            }
        }
        return true;
    }

    protected boolean getDefaultValue() {
        return true;
    }
}
