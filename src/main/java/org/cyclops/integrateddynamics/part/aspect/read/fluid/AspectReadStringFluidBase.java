package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadStringBase;

/**
 * Base class for string fluid aspects.
 * @author rubensworks
 */
public abstract class AspectReadStringFluidBase extends AspectReadStringBase {

    @Override
    protected String getUnlocalizedStringType() {
        return "fluid." + getUnlocalizedStringFluidType();
    }

    protected abstract String getUnlocalizedStringFluidType();

    protected abstract String getValue(FluidTankInfo[] tankInfo, IAspectProperties properties);

    protected String getDefaultValue() {
        return "";
    }

    @Override
    protected ValueTypeString.ValueString getValue(PartTarget target, IAspectProperties properties) {
        DimPos dimPos = target.getTarget().getPos();
        TileEntity tile = dimPos.getWorld().getTileEntity(dimPos.getBlockPos());
        if(tile instanceof IFluidHandler) {
            IFluidHandler fluidHandler = (IFluidHandler) tile;
            FluidTankInfo[] tankInfo = fluidHandler.getTankInfo(target.getTarget().getSide());
            return ValueTypeString.ValueString.of(getValue(tankInfo, properties));
        }
        return ValueTypeString.ValueString.of(getDefaultValue());
    }

}
