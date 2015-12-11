package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraftforge.fluids.FluidTankInfo;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;

/**
 * Aspect that checks the amount of different tanks are inside the target.
 * @author rubensworks
 */
public class AspectReadIntegerFluidTanks extends AspectReadIntegerFluidBase {

    @Override
    protected String getUnlocalizedIntegerFluidType() {
        return "tanks";
    }

    @Override
    protected int getValue(FluidTankInfo[] tankInfo, IAspectProperties properties) {
        return tankInfo.length;
    }

}
