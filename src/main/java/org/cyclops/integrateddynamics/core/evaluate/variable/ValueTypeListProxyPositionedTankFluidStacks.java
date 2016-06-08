package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;

/**
 * A list proxy for a tank's fluidstacks at a certain position.
 */
public class ValueTypeListProxyPositionedTankFluidStacks extends ValueTypeListProxyBase<ValueObjectTypeFluidStack, ValueObjectTypeFluidStack.ValueFluidStack> implements INBTProvider {

    @NBTPersist
    private DimPos pos;
    @NBTPersist
    private EnumFacing side;

    public ValueTypeListProxyPositionedTankFluidStacks() {
        this(null, null);
    }

    public ValueTypeListProxyPositionedTankFluidStacks(DimPos pos, EnumFacing side) {
        super(ValueTypeListProxyFactories.POSITIONED_TANK_FLUIDSTACKS.getName(), ValueTypes.OBJECT_FLUIDSTACK);
        this.pos = pos;
        this.side = side;
    }

    protected IFluidHandler getTank() {
        return TileHelpers.getCapability(pos, side, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
    }

    @Override
    public int getLength() {
        IFluidHandler tank = getTank();
        if(tank == null) {
            return 0;
        }
        IFluidTankProperties[] tanks = tank.getTankProperties();
        if(tanks == null) {
            return 0;
        }
        return tanks.length;
    }

    @Override
    public ValueObjectTypeFluidStack.ValueFluidStack get(int index) {
        return ValueObjectTypeFluidStack.ValueFluidStack.of(getTank().getTankProperties()[index].getContents());
    }

    @Override
    public void writeGeneratedFieldsToNBT(NBTTagCompound tag) {

    }

    @Override
    public void readGeneratedFieldsFromNBT(NBTTagCompound tag) {

    }
}
