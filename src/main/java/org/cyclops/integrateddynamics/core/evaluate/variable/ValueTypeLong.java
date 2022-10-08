package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.LongTag;
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
public class ValueTypeLong extends ValueTypeBase<ValueTypeLong.ValueLong> implements IValueTypeNumber<ValueTypeLong.ValueLong> {

    public ValueTypeLong() {
        super("long", Helpers.RGBToInt(215, 254, 23), ChatFormatting.YELLOW, ValueTypeLong.ValueLong.class);
    }

    @Override
    public ValueLong getDefault() {
        return ValueLong.of(0L);
    }

    @Override
    public MutableComponent toCompactString(ValueLong value) {
        return Component.literal(Long.toString(value.getRawValue()));
    }

    @Override
    public Tag serialize(ValueLong value) {
        return LongTag.valueOf(value.getRawValue());
    }

    @Override
    public ValueLong deserialize(Tag value) {
        if (value.getId() == Tag.TAG_LONG) {
            return ValueLong.of(((LongTag) value).getAsLong());
        } else {
            throw new IllegalArgumentException(String.format("Value \"%s\" could not be parsed to a long.", value));
        }
    }

    @Override
    public String toString(ValueLong value) {
        return Long.toString(value.getRawValue());
    }

    @Override
    public ValueLong parseString(String value) throws EvaluationException {
        try {
            return ValueLong.of(Long.parseLong(value));
        } catch (NumberFormatException e) {
            throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_PARSE, value,
                    Component.translatable(getTranslationKey())));
        }
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
    public ValueTypeInteger.ValueInteger round(ValueLong a) {
        return ValueTypeInteger.ValueInteger.of((int) a.getRawValue());
    }

    @Override
    public ValueTypeInteger.ValueInteger ceil(ValueLong a) {
        return ValueTypeInteger.ValueInteger.of((int) a.getRawValue());
    }

    @Override
    public ValueTypeInteger.ValueInteger floor(ValueLong a) {
        return ValueTypeInteger.ValueInteger.of((int) a.getRawValue());
    }

    @Override
    public ValueTypeString.ValueString compact(ValueLong a) {
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
    public String getName(ValueLong a) {
        return toCompactString(a).getString();
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
