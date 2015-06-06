package org.cyclops.integrateddynamics.core.evaluate.variable;

/**
 * Base implementation of a value type.
 * @author rubensworks
 */
public abstract class BaseValueType<V extends IValue> implements IValueType<V> {

    private final String typeName;
    private final int color;

    public BaseValueType(String typeName, int color) {
        this.typeName = typeName;
        this.color = color;
    }

    @Override
    public String getTypeName() {
        return this.typeName;
    }

    @Override
    public int getDisplayColor() {
        return this.color;
    }
}
