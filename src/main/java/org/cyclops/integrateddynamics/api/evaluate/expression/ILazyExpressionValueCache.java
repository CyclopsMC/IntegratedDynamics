package org.cyclops.integrateddynamics.api.evaluate.expression;

import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;

/**
 * Cache object that is responsible for storing values of this expression.
 * This cache object is responsible for determining when the values need to be ejected from the cache.
 * @author rubensworks
 */
public interface ILazyExpressionValueCache {

    public void setValue(int id, IValue value);
    public boolean hasValue(int id);
    public IValue getValue(int id);

}
