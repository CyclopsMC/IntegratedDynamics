package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraftforge.fluids.FluidTankInfo;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;

/**
 * Aspect that checks the target tank total amount.
 * @author rubensworks
 */
public class AspectReadIntegerFluidAmountTotal extends AspectReadIntegerFluidBase {

    @Override
    protected String getUnlocalizedIntegerFluidType() {
        return "totalamount";
    }

    @Override
    protected int getValue(FluidTankInfo[] tankInfo, IAspectProperties properties) {
        int amount = 0;
        for(FluidTankInfo tank : tankInfo) {
            if(tank.fluid != null) {
                amount += tank.fluid.amount;
            }
        }
        return amount;
    }

}
