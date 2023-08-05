package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNumber;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Value type with values that are doubles.
 * @author rubensworks
 */
public class ValueTypeDouble extends ValueTypeBase<ValueTypeDouble.ValueDouble> implements IValueTypeNumber<ValueTypeDouble.ValueDouble> {

    public ValueTypeDouble() {
        super("double", Helpers.RGBToInt(235, 234, 23), ChatFormatting.YELLOW, ValueTypeDouble.ValueDouble.class);
    }

    @Override
    public ValueDouble getDefault() {
        return ValueDouble.of(0D);
    }

    @Override
    public MutableComponent toCompactString(ValueDouble value) {
        return Component.literal(Double.toString(value.getRawValue()));
    }

    @Override
    public Tag serialize(ValueDouble value) {
        return DoubleTag.valueOf(value.getRawValue());
    }

    @Override
    public ValueDouble deserialize(Tag value) {
        if (value.getId() == Tag.TAG_DOUBLE) {
            return ValueDouble.of(((DoubleTag) value).getAsDouble());
        } else {
            throw new IllegalArgumentException(String.format("Value \"%s\" could not be parsed to a double.", value));
        }
    }

    @Override
    public String toString(ValueDouble value) {
        return Double.toString(value.getRawValue());
    }

    @Override
    public ValueDouble parseString(String value) throws EvaluationException {
        try {
            return ValueDouble.of(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_PARSE, value,
                    Component.translatable(getTranslationKey())));
        }
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

    @Override
    public ValueDouble increment(ValueDouble a) {
        return ValueDouble.of(a.getRawValue() + 1D);
    }

    @Override
    public ValueDouble decrement(ValueDouble a) {
        return ValueDouble.of(a.getRawValue() - 1D);
    }

    @Override
    public ValueDouble modulus(ValueDouble a, ValueDouble b) {
        return ValueDouble.of(a.getRawValue() % b.getRawValue());
    }

    @Override
    public boolean greaterThan(ValueDouble a, ValueDouble b) {
        return a.getRawValue() > b.getRawValue();
    }

    @Override
    public boolean lessThan(ValueDouble a, ValueDouble b) {
        return a.getRawValue() < b.getRawValue();
    }

    @Override
    public ValueTypeInteger.ValueInteger round(ValueDouble a) {
        return ValueTypeInteger.ValueInteger.of((int) Math.round(a.getRawValue()));
    }

    @Override
    public ValueTypeInteger.ValueInteger ceil(ValueDouble a) {
        return ValueTypeInteger.ValueInteger.of((int) Math.ceil(a.getRawValue()));
    }

    @Override
    public ValueTypeInteger.ValueInteger floor(ValueDouble a) {
        return ValueTypeInteger.ValueInteger.of((int) Math.floor(a.getRawValue()));
    }

    @Override
    public ValueTypeString.ValueString compact(ValueDouble a) {
        NumberFormat nf = NumberFormat.getCompactNumberInstance(
            Locale.US,
            GeneralConfig.numberCompactUseLongStyle ? NumberFormat.Style.LONG : NumberFormat.Style.SHORT
        );
        nf.setMinimumFractionDigits(GeneralConfig.numberCompactMinimumFractionDigits);
        nf.setMaximumFractionDigits(GeneralConfig.numberCompactMaximumFractionDigits);
        nf.setMinimumIntegerDigits(GeneralConfig.numberCompactMinimumIntegerDigits);
        nf.setMaximumIntegerDigits(GeneralConfig.numberCompactMaximumIntegerDigits);
        return ValueTypeString.ValueString.of(nf.format(a.getRawValue()));
    }

    @Override
    public String getName(ValueDouble a) {
        return toCompactString(a).getString();
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

        @Override
        public int hashCode() {
            return getType().hashCode() + ((int) value * 100);
        }
    }

}
