package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;

/**
 * A list proxy for a tank's fluidstacks at a certain position.
 */
public class ValueTypeListProxyPositionedTankFluidStacks extends ValueTypeListProxyPositioned<ValueObjectTypeFluidStack, ValueObjectTypeFluidStack.ValueFluidStack> implements INBTProvider {

    public ValueTypeListProxyPositionedTankFluidStacks(DimPos pos, Direction side) {
        super(ValueTypeListProxyFactories.POSITIONED_TANK_FLUIDSTACKS.getName(), ValueTypes.OBJECT_FLUIDSTACK, pos, side);
    }

    public ValueTypeListProxyPositionedTankFluidStacks() {
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
    public ValueObjectTypeFluidStack.ValueFluidStack get(int index) {
        return ValueObjectTypeFluidStack.ValueFluidStack.of(getTank()
                .map(fluidHandler -> fluidHandler.getFluidInTank(index))
                .orElse(FluidStack.EMPTY));
    }
}
