package org.cyclops.integrateddynamics.part.aspect.read;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeDouble;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Base class for double read aspects.
 * @author rubensworks
 */
public abstract class AspectReadDoubleBase extends AspectReadBase<ValueTypeDouble.ValueDouble, ValueTypeDouble> {

    @Override
    public String getUnlocalizedType() {
        return super.getUnlocalizedType() + ".double." + getUnlocalizedDoubleType();
    }

    protected abstract String getUnlocalizedDoubleType();

    @Override
    public ValueTypeDouble getValueType() {
        return ValueTypes.DOUBLE;
    }

}
