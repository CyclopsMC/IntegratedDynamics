package org.cyclops.integrateddynamics.part.aspect.read;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Base class for boolean read aspects.
 * @author rubensworks
 */
public abstract class AspectReadBooleanBase extends AspectReadBase<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> {

    @Override
    public String getUnlocalizedType() {
        return super.getUnlocalizedType() + ".boolean." + getUnlocalizedBooleanType();
    }

    protected abstract String getUnlocalizedBooleanType();

    @Override
    public ValueTypeBoolean getValueType() {
        return ValueTypes.BOOLEAN;
    }

}
