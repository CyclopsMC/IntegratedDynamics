package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * Aspect that checks active tank fluid rarity.
 * @author rubensworks
 */
public class AspectReadStringFluidRarity extends AspectReadStringFluidActivatableFluidBase {

    @Override
    protected String getUnlocalizedStringFluidType() {
        return "rarity";
    }

    @Override
    protected String getValue(Fluid fluid, FluidStack fluidStack) {
        return fluid.getRarity(fluidStack).rarityName;
    }

}
