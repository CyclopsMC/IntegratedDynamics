package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;
import net.minecraft.util.EnumChatFormatting;
import org.cyclops.cyclopscore.helper.Helpers;

/**
 * Value type with values that are integers.
 * @author rubensworks
 */
public class ValueTypeInteger extends ValueTypeBase<ValueTypeInteger.ValueInteger> {

    public ValueTypeInteger() {
        super("integer", Helpers.RGBToInt(243, 150, 4), EnumChatFormatting.GOLD.toString());
    }

    @Override
    public ValueInteger getDefault() {
        return ValueInteger.of(0);
    }

    @Override
    public String toCompactString(ValueInteger value) {
        return Integer.toString(value.getRawValue());
    }

    @Override
    public String serialize(ValueInteger value) {
        return Integer.toString(value.getRawValue());
    }

    @Override
    public ValueInteger deserialize(String value) {
        return ValueInteger.of(Integer.parseInt(value));
    }

    @ToString
    public static class ValueInteger extends ValueBase {

        private final int value;

        private ValueInteger(int value) {
            super(ValueTypes.INTEGER);
            this.value = value;
        }

        public static ValueInteger of(int value) {
            return new ValueInteger(value);
        }

        public int getRawValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ValueInteger && ((ValueInteger) o).value == this.value;
        }
    }

}
