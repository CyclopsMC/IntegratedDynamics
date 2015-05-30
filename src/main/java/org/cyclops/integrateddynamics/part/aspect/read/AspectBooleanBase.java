package org.cyclops.integrateddynamics.part.aspect.read;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.util.List;

/**
 * Base class for boolean aspects.
 * @author rubensworks
 */
public abstract class AspectBooleanBase extends AspectBase<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> {

    @Override
    public String getUnlocalizedType() {
        return "boolean." + getUnlocalizedBooleanType();
    }

    protected abstract String getUnlocalizedBooleanType();

    @Override
    public void loadTooltip(List<String> lines) {

    }

    @Override
    public ValueTypeBoolean getValueType() {
        return ValueTypes.BOOLEAN;
    }

}
