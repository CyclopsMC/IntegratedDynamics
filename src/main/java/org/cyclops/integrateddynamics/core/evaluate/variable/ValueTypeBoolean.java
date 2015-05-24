package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;

/**
 * Value type with values 'true' or 'false'
 * @author rubensworks
 */
public class ValueTypeBoolean implements IValueType<ValueTypeBoolean.ValueBoolean> {

    @Override
    public ValueBoolean getDefault() {
        return ValueBoolean.of(false);
    }

    @Override
    public String getTypeName() {
        return "boolean";
    }

    @ToString
    public static class ValueBoolean extends BaseValue {

        private final boolean value;

        private ValueBoolean(boolean value) {
            super(ValueTypes.BOOLEAN);
            this.value = value;
        }

        public static ValueBoolean of(boolean value) {
            return new ValueBoolean(value);
        }

        public boolean getRawValue() {
            return value;
        }

    }

}
