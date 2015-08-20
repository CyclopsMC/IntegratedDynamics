package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;
import net.minecraft.util.EnumChatFormatting;
import org.cyclops.cyclopscore.helper.Helpers;

/**
 * Value type with values that are strings.
 * @author rubensworks
 */
public class ValueTypeString extends ValueTypeBase<ValueTypeString.ValueString> {

    public ValueTypeString() {
        super("string", Helpers.RGBToInt(250, 10, 13), EnumChatFormatting.RED.toString());
    }

    @Override
    public ValueString getDefault() {
        return ValueString.of("");
    }

    @Override
    public String toCompactString(ValueString value) {
        return value.getRawValue();
    }

    @Override
    public String serialize(ValueString value) {
        return value.getRawValue();
    }

    @Override
    public ValueString deserialize(String value) {
        return ValueString.of(value);
    }

    @ToString
    public static class ValueString extends BaseValue {

        private final String value;

        private ValueString(String value) {
            super(ValueTypes.STRING);
            this.value = value;
        }

        public static ValueString of(String value) {
            return new ValueString(value);
        }

        public String getRawValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ValueString && ((ValueString) o).value.equals(this.value);
        }
    }

}
