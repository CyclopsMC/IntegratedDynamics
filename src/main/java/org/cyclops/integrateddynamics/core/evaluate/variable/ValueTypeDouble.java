package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;
import net.minecraft.util.EnumChatFormatting;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNumber;

/**
 * Value type with values that are doubles.
 * @author rubensworks
 */
public class ValueTypeDouble extends ValueTypeBase<ValueTypeDouble.ValueDouble> implements IValueTypeNumber<ValueTypeDouble.ValueDouble> {

    public ValueTypeDouble() {
        super("double", Helpers.RGBToInt(235, 234, 23), EnumChatFormatting.YELLOW.toString());
    }

    @Override
    public ValueDouble getDefault() {
        return ValueDouble.of(0D);
    }

    @Override
    public String toCompactString(ValueDouble value) {
        return Double.toString(value.getRawValue());
    }

    @Override
    public String serialize(ValueDouble value) {
        return Double.toString(value.getRawValue());
    }

    @Override
    public ValueDouble deserialize(String value) {
        return ValueDouble.of(Double.parseDouble(value));
    }

    @Override
    public boolean isZero(ValueDouble a) {
        return a.getRawValue() == 0D;
    }

    @Override
    public boolean isOne(ValueDouble a) {
        return a.getRawValue() == 1D;
    }

    @Override
    public ValueDouble add(ValueDouble a, ValueDouble b) {
        return ValueDouble.of(a.getRawValue() + b.getRawValue());
    }

    @Override
    public ValueDouble subtract(ValueDouble a, ValueDouble b) {
        return ValueDouble.of(a.getRawValue() - b.getRawValue());
    }

    @Override
    public ValueDouble multiply(ValueDouble a, ValueDouble b) {
        return ValueDouble.of(a.getRawValue() * b.getRawValue());
    }

    @Override
    public ValueDouble divide(ValueDouble a, ValueDouble b) {
        return ValueDouble.of(a.getRawValue() / b.getRawValue());
    }

    @Override
    public ValueDouble max(ValueDouble a, ValueDouble b) {
        return ValueDouble.of(Math.max(a.getRawValue(), b.getRawValue()));
    }

    @Override
    public ValueDouble min(ValueDouble a, ValueDouble b) {
        return ValueDouble.of(Math.min(a.getRawValue(), b.getRawValue()));
    }

    @ToString
    public static class ValueDouble extends ValueBase {

        private final double value;

        private ValueDouble(double value) {
            super(ValueTypes.DOUBLE);
            this.value = value;
        }

        public static ValueDouble of(double value) {
            return new ValueDouble(value);
        }

        public double getRawValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ValueDouble && ((ValueDouble) o).value == this.value;
        }
    }

}
