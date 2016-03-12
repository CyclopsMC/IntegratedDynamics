package org.cyclops.integrateddynamics.api.evaluate.variable;

/**
 * A value type that can be null.
 * @author rubensworks
 */
public interface IValueTypeNullable<V extends IValue> extends IValueType<V> {

    public boolean isNull(V a);

}
