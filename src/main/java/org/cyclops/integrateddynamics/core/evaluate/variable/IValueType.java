package org.cyclops.integrateddynamics.core.evaluate.variable;

/**
 * Type of variable
 * @author rubensworks
 */
public interface IValueType<V extends IValue> {

    /**
     * Create an immutable default value.
     * @return The default value of this type.
     */
    public V getDefault();

    /**
     * @return The unique name of this type that will also be used for display.
     */
    public String getTypeName();

}
