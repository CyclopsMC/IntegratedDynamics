package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;
import net.minecraft.util.text.TextFormatting;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNumber;

/**
 * Value type with values that are doubles.
 * @author rubensworks
 */
public class ValueTypeLong extends ValueTypeBase<ValueTypeLong.ValueLong> implements IValueTypeNumber<ValueTypeLong.ValueLong> {

    public ValueTypeLong() {
        super("long", Helpers.RGBToInt(215, 254, 23), TextFormatting.YELLOW.toString());
    }

    @Override
    public ValueLong getDefault() {
        return ValueLong.of(0L);
    }

    @Override
    public String toCompactString(ValueLong value) {
        return Long.toString(value.getRawValue());
    }

    @Override
    public String serialize(ValueLong value) {
        return Long.toString(value.getRawValue());
    }

    @Override
    public ValueLong deserialize(String value) {
        return ValueLong.of(Long.parseLong(value));
    }

    @Override
    public boolean isZero(ValueLong a) {
        return a.getRawValue() == 0L;
    }

    @Override
    public boolean isOne(ValueLong a) {
        return a.getRawValue() == 1L;
    }

    @Override
    public ValueLong add(ValueLong a, ValueLong b) {
        return ValueLong.of(a.getRawValue() + b.getRawValue());
    }

    @Override
    public ValueLong subtract(ValueLong a, ValueLong b) {
        return ValueLong.of(a.getRawValue() - b.getRawValue());
    }

    @Override
    public ValueLong multiply(ValueLong a, ValueLong b) {
        return ValueLong.of(a.getRawValue() * b.getRawValue());
    }

    @Override
    public ValueLong divide(ValueLong a, ValueLong b) {
        return ValueLong.of(a.getRawValue() / b.getRawValue());
    }

    @Override
    public ValueLong max(ValueLong a, ValueLong b) {
        return ValueLong.of(Math.max(a.getRawValue(), b.getRawValue()));
    }

    @Override
    public ValueLong min(ValueLong a, ValueLong b) {
        return ValueLong.of(Math.min(a.getRawValue(), b.getRawValue()));
    }

    @Override
    public boolean greaterThan(ValueLong a, ValueLong b) {
        return a.getRawValue() > b.getRawValue();
    }

    @Override
    public boolean lessThan(ValueLong a, ValueLong b) {
        return a.getRawValue() < b.getRawValue();
    }

    @Override
    public String getName(ValueLong a) {
        return toCompactString(a);
    }

    @ToString
    public static class ValueLong extends ValueBase {

        private final long value;

        private ValueLong(long value) {
            super(ValueTypes.LONG);
            this.value = value;
        }

        public static ValueLong of(long value) {
            return new ValueLong(value);
        }

        public long getRawValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ValueLong && ((ValueLong) o).value == this.value;
        }

        @Override
        public int hashCode() {
            return getType().hashCode() + (int) value;
        }
    }

}
