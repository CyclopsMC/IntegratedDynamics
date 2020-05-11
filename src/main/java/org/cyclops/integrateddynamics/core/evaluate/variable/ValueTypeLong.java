package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNumber;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

/**
 * Value type with values that are doubles.
 * @author rubensworks
 */
public class ValueTypeLong extends ValueTypeBase<ValueTypeLong.ValueLong> implements IValueTypeNumber<ValueTypeLong.ValueLong> {

    public ValueTypeLong() {
        super("long", Helpers.RGBToInt(215, 254, 23), TextFormatting.YELLOW, ValueTypeLong.ValueLong.class);
    }

    @Override
    public ValueLong getDefault() {
        return ValueLong.of(0L);
    }

    @Override
    public ITextComponent toCompactString(ValueLong value) {
        return new StringTextComponent(Long.toString(value.getRawValue()));
    }

    @Override
    public INBT serialize(ValueLong value) {
        return LongNBT.valueOf(value.getRawValue());
    }

    @Override
    public ValueLong deserialize(INBT value) {
        if (value.getId() == Constants.NBT.TAG_LONG) {
            return ValueLong.of(((LongNBT) value).getLong());
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
            throw new EvaluationException(new TranslationTextComponent(L10NValues.OPERATOR_ERROR_PARSE, value,
                    new TranslationTextComponent(getTranslationKey())));
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
