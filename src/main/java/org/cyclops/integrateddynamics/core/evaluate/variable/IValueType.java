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

    /**
     * @param value The value
     * @return A short string representation used in guis to show the value.
     */
    public String toCompactString(V value);

    /**
     * @return The color that is used to identify this value type.
     */
    public int getDisplayColor();

}
