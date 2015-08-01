package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;
import net.minecraft.util.EnumChatFormatting;
import org.cyclops.cyclopscore.helper.Helpers;

/**
 * Value type with values 'true' or 'false'
 * @author rubensworks
 */
public class ValueTypeBoolean extends ValueTypeBase<ValueTypeBoolean.ValueBoolean> {

    public ValueTypeBoolean() {
        super("boolean", Helpers.RGBToInt(43, 47, 231), EnumChatFormatting.BLUE.toString());
    }

    @Override
    public ValueBoolean getDefault() {
        return ValueBoolean.of(false);
    }

    @Override
    public String toCompactString(ValueBoolean value) {
        return Boolean.toString(value.getRawValue());
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
