package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;
import net.minecraft.util.EnumChatFormatting;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNumber;

/**
 * Value type with values that are integers.
 * The raw value is nullable.
 * @author rubensworks
 */
public class ValueTypeInteger extends ValueTypeBase<ValueTypeInteger.ValueInteger> implements IValueTypeNumber<ValueTypeInteger.ValueInteger> {

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

    @Override
    public boolean isZero(ValueInteger a) {
        return a.getRawValue() == 0;
    }

    @Override
    public boolean isOne(ValueInteger a) {
        return a.getRawValue() == 1;
    }

    @Override
    public ValueInteger add(ValueInteger a, ValueInteger b) {
        return ValueInteger.of(a.getRawValue() + b.getRawValue());
    }

    @Override
    public ValueInteger subtract(ValueInteger a, ValueInteger b) {
        return ValueInteger.of(a.getRawValue() - b.getRawValue());
    }

    @Override
    public ValueInteger multiply(ValueInteger a, ValueInteger b) {
        return ValueInteger.of(a.getRawValue() * b.getRawValue());
    }

    @Override
    public ValueInteger divide(ValueInteger a, ValueInteger b) {
        return ValueInteger.of(a.getRawValue() / b.getRawValue());
    }

    @Override
    public ValueInteger max(ValueInteger a, ValueInteger b) {
        return ValueInteger.of(Math.max(a.getRawValue(), b.getRawValue()));
    }

    @Override
    public ValueInteger min(ValueInteger a, ValueInteger b) {
        return ValueInteger.of(Math.min(a.getRawValue(), b.getRawValue()));
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
