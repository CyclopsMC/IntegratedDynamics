package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.IModHelpersNeoForge;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;

import java.util.Optional;

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

    protected Optional<ResourceHandler<FluidResource>> getTank() {
        return IModHelpersNeoForge.get().getCapabilityHelpers().getCapability(getPos(), getSide(), net.neoforged.neoforge.capabilities.Capabilities.Fluid.BLOCK);
    }

    @Override
    public int getLength() {
        return getTank()
                .map(ResourceHandler<FluidResource>::size)
                .orElse(0);
    }

    @Override
    public ValueTypeInteger.ValueInteger get(int index) {
        return ValueTypeInteger.ValueInteger.of(getTank()
                .map(fluidHandler -> fluidHandler.getCapacityAsInt(index, FluidResource.EMPTY))
                .orElse(0));
    }
}
