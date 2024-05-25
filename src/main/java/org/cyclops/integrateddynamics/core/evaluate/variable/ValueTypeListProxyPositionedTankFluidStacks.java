package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;

import java.util.Optional;

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

    protected Optional<IFluidHandler> getTank() {
        return BlockEntityHelpers.getCapability(getPos(), getSide(), net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK);
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
