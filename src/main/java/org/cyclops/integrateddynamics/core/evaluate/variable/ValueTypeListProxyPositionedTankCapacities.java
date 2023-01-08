package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;

/**
 * A list proxy for a tank's capacities at a certain position.
 */
public class ValueTypeListProxyPositionedTankCapacities extends ValueTypeListProxyPositioned<ValueTypeInteger, ValueTypeInteger.ValueInteger> implements INBTProvider {

    public ValueTypeListProxyPositionedTankCapacities(DimPos pos, Direction side) {
        super(ValueTypeListProxyFactories.POSITIONED_TANK_CAPACITIES.getName(), ValueTypes.INTEGER, pos, side);
    }

    public ValueTypeListProxyPositionedTankCapacities() {
        this(null, null);
    }

    protected LazyOptional<IFluidHandler> getTank() {
        return BlockEntityHelpers.getCapability(getPos(), getSide(), ForgeCapabilities.FLUID_HANDLER);
    }

    @Override
    public int getLength() {
        return getTank()
                .map(IFluidHandler::getTanks)
                .orElse(0);
    }

    @Override
    public ValueTypeInteger.ValueInteger get(int index) {
        return ValueTypeInteger.ValueInteger.of(getTank()
                .map(fluidHandler -> fluidHandler.getTankCapacity(index))
                .orElse(0));
    }
}
