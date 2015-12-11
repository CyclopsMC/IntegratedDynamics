package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadIntegerBase;

/**
 * Base class for integer fluid aspects.
 * @author rubensworks
 */
public abstract class AspectReadIntegerFluidBase extends AspectReadIntegerBase {

    @Override
    protected String getUnlocalizedIntegerType() {
        return "fluid." + getUnlocalizedIntegerFluidType();
    }

    protected abstract String getUnlocalizedIntegerFluidType();

    protected abstract int getValue(FluidTankInfo[] tankInfo, IAspectProperties properties);

    protected int getDefaultValue() {
        return 0;
    }

    @Override
    protected ValueTypeInteger.ValueInteger getValue(PartTarget target, IAspectProperties properties) {
        DimPos dimPos = target.getTarget().getPos();
        TileEntity tile = dimPos.getWorld().getTileEntity(dimPos.getBlockPos());
        if(tile instanceof IFluidHandler) {
            IFluidHandler fluidHandler = (IFluidHandler) tile;
            FluidTankInfo[] tankInfo = fluidHandler.getTankInfo(target.getTarget().getSide());
            return ValueTypeInteger.ValueInteger.of(getValue(tankInfo, properties));
        }
        return ValueTypeInteger.ValueInteger.of(getDefaultValue());
    }

}
