package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;

/**
 * A list proxy for a tank's capacities at a certain position.
 */
public class ValueTypeListProxyPositionedTankCapacities extends ValueTypeListProxyPositioned<ValueTypeInteger, ValueTypeInteger.ValueInteger> implements INBTProvider {

    public ValueTypeListProxyPositionedTankCapacities(DimPos pos, EnumFacing side) {
        super(ValueTypeListProxyFactories.POSITIONED_TANK_CAPACITIES.getName(), ValueTypes.INTEGER, pos, side);
    }

    public ValueTypeListProxyPositionedTankCapacities() {
        this(null, null);
    }

    protected IFluidHandler getTank() {
        return TileHelpers.getCapability(getPos(), getSide(), CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
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
    public ValueTypeInteger.ValueInteger get(int index) {
        return ValueTypeInteger.ValueInteger.of(getTank().getTankProperties()[index].getCapacity());
    }
}
