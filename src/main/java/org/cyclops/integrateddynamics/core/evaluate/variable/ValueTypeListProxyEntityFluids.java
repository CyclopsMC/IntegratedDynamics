package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;

import javax.annotation.Nullable;

/**
 * A list proxy for the fluid handler fluids of an entity.
 */
public class ValueTypeListProxyEntityFluids extends ValueTypeListProxyEntityCapability<ResourceHandler<FluidResource>, Direction, ValueObjectTypeFluidStack, ValueObjectTypeFluidStack.ValueFluidStack> implements INBTProvider {

    public ValueTypeListProxyEntityFluids(Level world, Entity entity, @Nullable Direction side) {
        super(ValueTypeListProxyFactories.ENTITY_CAPABILITY_FLUIDS.getName(), ValueTypes.OBJECT_FLUIDSTACK,
                world, entity, Capabilities.Fluid.ENTITY, side);
    }

    public ValueTypeListProxyEntityFluids() {
        this(null, null, null);
    }

    @Override
    public int getLength() {
        return getCapability()
                .map(ResourceHandler<FluidResource>::size)
                .orElse(0);
    }

    @Override
    public ValueObjectTypeFluidStack.ValueFluidStack get(int index) {
        return ValueObjectTypeFluidStack.ValueFluidStack.of(getCapability()
                .map(handler -> handler.getResource(index).toStack(handler.getAmountAsInt(index)))
                .orElse(FluidStack.EMPTY));
    }
}
