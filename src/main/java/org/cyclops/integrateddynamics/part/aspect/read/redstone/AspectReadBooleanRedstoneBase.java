package org.cyclops.integrateddynamics.part.aspect.read.redstone;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBooleanBase;

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
    protected ValueTypeBoolean.ValueBoolean getValue(PartTarget target, IAspectProperties properties) {
        DimPos dimPos = target.getTarget().getPos();
        boolean value = AspectReadBooleanRedstoneBase.this.getValue(dimPos.getWorld().getRedstonePower(
                dimPos.getBlockPos(), target.getCenter().getSide()));
        return ValueTypeBoolean.ValueBoolean.of(value);
    }

    protected abstract boolean getValue(int redstoneLevel);

}
