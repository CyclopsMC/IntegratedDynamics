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
public abstract class AspectBooleanRedstoneBase extends AspectBooleanBase {

    @Override
    public IAspectVariable<ValueTypeBoolean.ValueBoolean> createNewVariable(final PartTarget target) {
        return new LazyAspectVariable<ValueTypeBoolean.ValueBoolean>(getValueType(), target) {
            @Override
            public ValueTypeBoolean.ValueBoolean getValueLazy() {
                DimPos dimPos = target.getDimPosTarget();
                boolean value = AspectBooleanRedstoneBase.this.getValue(dimPos.getWorld().getRedstonePower(
                        dimPos.getBlockPos(), target.getSideCenter()));
                return ValueTypeBoolean.ValueBoolean.of(value);
            }
        };
    }

    protected abstract boolean getValue(int redstoneLevel);

}
