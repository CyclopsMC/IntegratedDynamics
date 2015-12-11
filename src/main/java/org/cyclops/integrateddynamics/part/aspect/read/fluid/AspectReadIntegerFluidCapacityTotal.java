package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraftforge.fluids.FluidTankInfo;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;

/**
 * Aspect that checks the target tank total capacity.
 * @author rubensworks
 */
public class AspectReadIntegerFluidCapacityTotal extends AspectReadIntegerFluidBase {

    @Override
    protected String getUnlocalizedIntegerFluidType() {
        return "totalcapacity";
    }

    @Override
    protected int getValue(FluidTankInfo[] tankInfo, IAspectProperties properties) {
        int capacity = 0;
        for(FluidTankInfo tank : tankInfo) {
            capacity += tank.capacity;
        }
        return capacity;
    }

}
