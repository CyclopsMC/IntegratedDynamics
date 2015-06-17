package org.cyclops.integrateddynamics.part.aspect.read;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Base class for integer read aspects.
 * @author rubensworks
 */
public abstract class AspectReadIntegerBase extends AspectReadBase<ValueTypeInteger.ValueInteger, ValueTypeInteger> {

    @Override
    public String getUnlocalizedType() {
        return super.getUnlocalizedType() + ".integer." + getUnlocalizedIntegerType();
    }

    protected abstract String getUnlocalizedIntegerType();

    @Override
    public ValueTypeInteger getValueType() {
        return ValueTypes.INTEGER;
    }

}
