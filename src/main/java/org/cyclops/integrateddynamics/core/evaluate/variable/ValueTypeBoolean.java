package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;
import net.minecraft.util.text.TextFormatting;
import org.cyclops.cyclopscore.helper.Helpers;

/**
 * Value type with values 'true' or 'false'
 * @author rubensworks
 */
public class ValueTypeBoolean extends ValueTypeBase<ValueTypeBoolean.ValueBoolean> {

    public ValueTypeBoolean() {
        super("boolean", Helpers.RGBToInt(43, 47, 231), TextFormatting.BLUE.toString());
    }

    @Override
    public ValueBoolean getDefault() {
        return ValueBoolean.of(false);
    }

    @Override
    public String toCompactString(ValueBoolean value) {
        return Boolean.toString(value.getRawValue());
    }

    @Override
    public String serialize(ValueBoolean value) {
        return Boolean.toString(value.getRawValue());
    }

    @Override
    public ValueBoolean deserialize(String value) {
        boolean b;
        if("true".equalsIgnoreCase(value) || "1".equals(value)) {
            b = true;
        } else if("false".equalsIgnoreCase(value) || "0".equals(value)) {
            b = false;
        } else {
            throw new IllegalArgumentException(String.format("Value \"%s\" could not be parsed to a boolean.", value));
        }
        return ValueBoolean.of(b);
    }

    @ToString
    public static class ValueBoolean extends ValueBase {

        private static final ValueBoolean TRUE = new ValueBoolean(true);
        private static final ValueBoolean FALSE = new ValueBoolean(false);

        private final boolean value;

        private ValueBoolean(boolean value) {
            super(ValueTypes.BOOLEAN);
            this.value = value;
        }

        public static ValueBoolean of(boolean value) {
            return value ? TRUE : FALSE;
        }

        public boolean getRawValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ValueBoolean && ((ValueBoolean) o).value == this.value;
        }
    }

}
