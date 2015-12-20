package org.cyclops.integrateddynamics.part.aspect.read;

import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeLong;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * Base class for long read aspects.
 * @author rubensworks
 */
public abstract class AspectReadLongBase extends AspectReadBase<ValueTypeLong.ValueLong, ValueTypeLong> {

    @Override
    public String getUnlocalizedType() {
        return super.getUnlocalizedType() + ".long." + getUnlocalizedLongType();
    }

    protected abstract String getUnlocalizedLongType();

    @Override
    public ValueTypeLong getValueType() {
        return ValueTypes.LONG;
    }

}
