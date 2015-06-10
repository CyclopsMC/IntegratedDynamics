package org.cyclops.integrateddynamics.part.aspect.read;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectVariable;
import org.cyclops.integrateddynamics.core.part.aspect.LazyAspectVariable;

/**
 * Base class for boolean redstone aspects.
 * @author rubensworks
 */
public abstract class AspectReadBooleanRedstoneBase extends AspectReadBooleanBase {

    @Override
    protected String getUnlocalizedBooleanType() {
        return "redstone." + getUnlocalizedBooleanRedstoneType();
    }

    protected abstract String getUnlocalizedBooleanRedstoneType();

    @Override
    public IAspectVariable<ValueTypeBoolean.ValueBoolean> createNewVariable(final PartTarget target) {
        return new LazyAspectVariable<ValueTypeBoolean.ValueBoolean>(getValueType(), target) {
            @Override
            public ValueTypeBoolean.ValueBoolean getValueLazy() {
                DimPos dimPos = target.getTarget().getPos();
                boolean value = AspectReadBooleanRedstoneBase.this.getValue(dimPos.getWorld().getRedstonePower(
                        dimPos.getBlockPos(), target.getCenter().getSide()));
                return ValueTypeBoolean.ValueBoolean.of(value);
            }
        };
    }

    protected abstract boolean getValue(int redstoneLevel);

}
