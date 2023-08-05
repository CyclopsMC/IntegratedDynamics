package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNumber;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

/**
 * Value type with values that are integers.
 * The raw value is nullable.
 * @author rubensworks
 */
public class ValueTypeInteger extends ValueTypeBase<ValueTypeInteger.ValueInteger> implements IValueTypeNumber<ValueTypeInteger.ValueInteger> {

    public ValueTypeInteger() {
        super("integer", Helpers.RGBToInt(243, 150, 4), ChatFormatting.GOLD, ValueTypeInteger.ValueInteger.class);
    }

    @Override
    public ValueInteger getDefault() {
        return ValueInteger.of(0);
    }

    @Override
    public MutableComponent toCompactString(ValueInteger value) {
        return new TextComponent(Integer.toString(value.getRawValue()));
    }

    @Override
    public Tag serialize(ValueInteger value) {
        return IntTag.valueOf(value.getRawValue());
    }

    @Override
    public ValueInteger deserialize(Tag value) {
        if (value.getId() == Tag.TAG_INT) {
            return ValueInteger.of(((IntTag) value).getAsInt());
        } else {
            throw new IllegalArgumentException(String.format("Value \"%s\" could not be parsed to an integer.", value));
        }
    }

    @Override
    public String toString(ValueInteger value) {
        return Integer.toString(value.getRawValue());
    }

    @Override
    public ValueInteger parseString(String value) throws EvaluationException {
        try {
            return ValueInteger.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            throw new EvaluationException(new TranslatableComponent(L10NValues.OPERATOR_ERROR_PARSE, value,
                    new TranslatableComponent(getTranslationKey())));
        }
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

    @Override
    public ValueInteger increment(ValueInteger a) {
        return ValueInteger.of(a.getRawValue() + 1);
    }

    @Override
    public ValueInteger decrement(ValueInteger a) {
        return ValueInteger.of(a.getRawValue() - 1);
    }

    @Override
    public ValueInteger modulus(ValueInteger a, ValueInteger b) {
        return ValueInteger.of(a.getRawValue() % b.getRawValue());
    }

    @Override
    public boolean greaterThan(ValueInteger a, ValueInteger b) {
        return a.getRawValue() > b.getRawValue();
    }

    @Override
    public boolean lessThan(ValueInteger a, ValueInteger b) {
        return a.getRawValue() < b.getRawValue();
    }

    @Override
    public ValueInteger round(ValueInteger a) {
        return a;
    }

    @Override
    public ValueInteger ceil(ValueInteger a) {
        return a;
    }

    @Override
    public ValueInteger floor(ValueInteger a) {
        return a;
    }

    @Override
    public String getName(ValueInteger a) {
        return toCompactString(a).getString();
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

        @Override
        public int hashCode() {
            return getType().hashCode() + value;
        }
    }

}
