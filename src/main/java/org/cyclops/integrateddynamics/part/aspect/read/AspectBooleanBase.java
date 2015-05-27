package org.cyclops.integrateddynamics.part.aspect.read;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;

/**
 * Base class for boolean aspects.
 * @author rubensworks
 */
public abstract class AspectBooleanBase implements IAspect<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> {

    @Override
    public ValueTypeBoolean getValueType() {
        return ValueTypes.BOOLEAN;
    }

}
