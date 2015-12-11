package org.cyclops.integrateddynamics.api.evaluate.variable;

/**
 * An immutable value.
 * @author rubensworks
 */
public interface IValue {

    /**
     * @return The type of this variable.
     */
    public IValueType getType();

    /**
     * Safely downcast this value.
     * @param valueType The type of value to cast to.
     * @param <V> The actual value type.
     * @return The downcasted value.
     * @throws IValueCastRegistry.ValueCastException If this value could not be cast to the given type.
     */
    public <V extends IValue> V cast(IValueType<V> valueType) throws IValueCastRegistry.ValueCastException;

    /**
     * Safely downcast this value.
     * @param valueType The type of value to cast to.
     * @param <V> The actual value type.
     * @return If this value can be casted to the target type.
     */
    public <V extends IValue> boolean canCast(IValueType<V> valueType);

}
