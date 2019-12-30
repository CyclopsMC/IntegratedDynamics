package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;

import javax.annotation.Nullable;

/**
 * A list proxy for the fluid handler fluids of an entity.
 */
public class ValueTypeListProxyEntityFluids extends ValueTypeListProxyEntityCapability<IFluidHandler, ValueObjectTypeFluidStack, ValueObjectTypeFluidStack.ValueFluidStack> implements INBTProvider {

    public ValueTypeListProxyEntityFluids(World world, Entity entity, @Nullable Direction side) {
        super(ValueTypeListProxyFactories.ENTITY_CAPABILITY_FLUIDS.getName(), ValueTypes.OBJECT_FLUIDSTACK,
                world, entity, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
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
