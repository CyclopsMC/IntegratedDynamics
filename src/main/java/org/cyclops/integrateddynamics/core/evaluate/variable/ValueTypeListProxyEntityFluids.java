package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;

import javax.annotation.Nullable;

/**
 * A list proxy for the fluid handler fluids of an entity.
 */
public class ValueTypeListProxyEntityFluids extends ValueTypeListProxyEntityCapability<IFluidHandlerItem, ValueObjectTypeFluidStack, ValueObjectTypeFluidStack.ValueFluidStack> implements INBTProvider {

    public ValueTypeListProxyEntityFluids(World world, Entity entity, @Nullable EnumFacing side) {
        super(ValueTypeListProxyFactories.ENTITY_CAPABILITY_FLUIDS.getName(), ValueTypes.OBJECT_FLUIDSTACK,
                world, entity, CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, side);
    }

    public ValueTypeListProxyEntityFluids() {
        this(null, null, null);
    }

    @Override
    public int getLength() {
        return getCapability().map(handler -> handler.getTankProperties().length).orElse(0);
    }

    @Override
    public ValueObjectTypeFluidStack.ValueFluidStack get(int index) {
        return ValueObjectTypeFluidStack.ValueFluidStack.of(getCapability().map(handler -> handler.getTankProperties()[index].getContents()).orElse(null));
    }
}
