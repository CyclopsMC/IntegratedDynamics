package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import com.google.common.collect.Sets;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeBlock;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadObjectBlockBase;

/**
 * Read a block from the world.
 * @author rubensworks
 */
public class AspectReadObjectBlockFluid extends AspectReadObjectBlockBase {

    @Override
    protected String getUnlocalizedBlockType() {
        return "fluid";
    }

    protected IBlockState getValue(FluidTankInfo[] tankInfo, IAspectProperties properties) {
        int i = getActiveTank(properties);
        if(i < tankInfo.length) {
            return getValue(tankInfo[i]);
        }
        return getDefaultValue();
    }

    protected IBlockState getValue(FluidTankInfo tankInfo) {
        IBlockState value = getDefaultValue();
        FluidStack fluidStack = tankInfo.fluid;
        if(fluidStack != null) {
            value = fluidStack.getFluid().getBlock().getDefaultState();
        }
        return value;
    }

    protected int getActiveTank(IAspectProperties properties) {
        return properties.getValue(AspectReadIntegerFluidActivatableBase.PROP_TANKID).getRawValue();
    }

    @Override
    protected IAspectProperties createDefaultProperties() {
        IAspectProperties properties = new AspectProperties(Sets.<IAspectPropertyTypeInstance>newHashSet(
                AspectReadIntegerFluidActivatableBase.PROP_TANKID
        ));
        properties.setValue(AspectReadIntegerFluidActivatableBase.PROP_TANKID, ValueTypeInteger.ValueInteger.of(0)); // Not required in this case, but we do this here just as an example on how to set default values.
        return properties;
    }

    @Override
    protected ValueObjectTypeBlock.ValueBlock getValue(PartTarget target, IAspectProperties properties) {
        DimPos dimPos = target.getTarget().getPos();
        TileEntity tile = dimPos.getWorld().getTileEntity(dimPos.getBlockPos());
        if(tile instanceof IFluidHandler) {
            IFluidHandler fluidHandler = (IFluidHandler) tile;
            FluidTankInfo[] tankInfo = fluidHandler.getTankInfo(target.getTarget().getSide());
            return ValueObjectTypeBlock.ValueBlock.of(getValue(tankInfo, properties));
        }
        return ValueObjectTypeBlock.ValueBlock.of(getDefaultValue());
    }

    private IBlockState getDefaultValue() {
        return ValueTypes.OBJECT_BLOCK.getDefault().getRawValue().get();
    }
}
