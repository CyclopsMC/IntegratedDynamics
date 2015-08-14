package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.part.PartTarget;

/**
 * Aspect that checks if the target is a fluid tank.
 * @author rubensworks
 */
public class AspectReadBooleanFluidApplicable extends AspectReadBooleanFluidBase {

    @Override
    protected String getUnlocalizedBooleanFluidType() {
        return "applicable";
    }

    @Override
    protected boolean getValue(FluidTankInfo[] tankInfo) {
        return false; // Not called here
    }

    @Override
    protected ValueTypeBoolean.ValueBoolean getValue(PartTarget target) {
        DimPos dimPos = target.getTarget().getPos();
        TileEntity tile = dimPos.getWorld().getTileEntity(dimPos.getBlockPos());
        return ValueTypeBoolean.ValueBoolean.of(tile instanceof IFluidHandler);
    }
}
