package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraftforge.fluids.FluidTankInfo;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;

/**
 * Aspect that checks if the target tank is empty.
 * @author rubensworks
 */
public class AspectReadBooleanFluidNonEmpty extends AspectReadBooleanFluidBase {

    @Override
    protected String getUnlocalizedBooleanFluidType() {
        return "nonempty";
    }

    @Override
    protected boolean getValue(FluidTankInfo[] tankInfo, IAspectProperties properties) {
        boolean hasFluid = false;
        for(FluidTankInfo tank : tankInfo) {
            if(tank.fluid != null && tank.fluid.amount > 0) {
                hasFluid = true;
            }
        }
        return hasFluid;
    }

}
