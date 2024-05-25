package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;

import javax.annotation.Nullable;

/**
 * A list proxy for the fluid handler fluids of an entity.
 */
public class ValueTypeListProxyEntityFluids extends ValueTypeListProxyEntityCapability<IFluidHandler, Direction, ValueObjectTypeFluidStack, ValueObjectTypeFluidStack.ValueFluidStack> implements INBTProvider {

    public ValueTypeListProxyEntityFluids(Level world, Entity entity, @Nullable Direction side) {
        super(ValueTypeListProxyFactories.ENTITY_CAPABILITY_FLUIDS.getName(), ValueTypes.OBJECT_FLUIDSTACK,
                world, entity, Capabilities.FluidHandler.ENTITY, side);
    }

    public ValueTypeListProxyEntityFluids() {
        this(null, null, null);
    }

    @Override
    public int getLength() {
        return getCapability()
                .map(IFluidHandler::getTanks)
                .orElse(0);
    }

    @Override
    public ValueObjectTypeFluidStack.ValueFluidStack get(int index) {
        return ValueObjectTypeFluidStack.ValueFluidStack.of(getCapability()
                .map(handler -> handler.getFluidInTank(index))
                .orElse(FluidStack.EMPTY));
    }
}
