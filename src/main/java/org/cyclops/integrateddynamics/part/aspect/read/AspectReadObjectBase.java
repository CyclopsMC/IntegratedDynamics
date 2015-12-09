package org.cyclops.integrateddynamics.part.aspect.read;

import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;

/**
 * Base class for object read aspects.
 * @author rubensworks
 */
public abstract class AspectReadObjectBase<V extends IValue, T extends IValueType<V>> extends AspectReadBase<V, T> {

    @Override
    public String getUnlocalizedType() {
        return super.getUnlocalizedType() + ".object." + getUnlocalizedObjectType();
    }

    protected abstract String getUnlocalizedObjectType();

}
