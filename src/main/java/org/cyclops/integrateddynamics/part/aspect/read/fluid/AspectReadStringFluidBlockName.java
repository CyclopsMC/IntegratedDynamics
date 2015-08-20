package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraft.block.Block;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * Aspect that checks active tank fluid block name.
 * @author rubensworks
 */
public class AspectReadStringFluidBlockName extends AspectReadStringFluidActivatableFluidBase {

    @Override
    protected String getUnlocalizedStringFluidType() {
        return "blockname";
    }

    @Override
    protected String getValue(Fluid fluid, FluidStack fluidStack) {
        Block block = fluid.getBlock();
        if(block != null) {
            return block.getLocalizedName();
        }
        return "";
    }

}
