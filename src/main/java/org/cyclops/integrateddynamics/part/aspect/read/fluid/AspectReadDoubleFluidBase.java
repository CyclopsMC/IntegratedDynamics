package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeDouble;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadDoubleBase;

/**
 * Base class for integer fluid aspects.
 * @author rubensworks
 */
public abstract class AspectReadDoubleFluidBase extends AspectReadDoubleBase {

    @Override
    protected String getUnlocalizedDoubleType() {
        return "fluid." + getUnlocalizedDoubleFluidType();
    }

    protected abstract String getUnlocalizedDoubleFluidType();

    protected abstract double getValue(FluidTankInfo[] tankInfo, AspectProperties properties);

    protected double getDefaultValue() {
        return 0;
    }

    @Override
    protected ValueTypeDouble.ValueDouble getValue(PartTarget target, AspectProperties properties) {
        DimPos dimPos = target.getTarget().getPos();
        TileEntity tile = dimPos.getWorld().getTileEntity(dimPos.getBlockPos());
        if(tile instanceof IFluidHandler) {
            IFluidHandler fluidHandler = (IFluidHandler) tile;
            FluidTankInfo[] tankInfo = fluidHandler.getTankInfo(target.getTarget().getSide());
            return ValueTypeDouble.ValueDouble.of(getValue(tankInfo, properties));
        }
        return ValueTypeDouble.ValueDouble.of(getDefaultValue());
    }

}
