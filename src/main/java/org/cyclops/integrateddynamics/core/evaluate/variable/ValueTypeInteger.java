package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNumber;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Value type with values that are integers.
 * The raw value is nullable.
 * @author rubensworks
 */
public class ValueTypeInteger extends ValueTypeBase<ValueTypeInteger.ValueInteger> implements IValueTypeNumber<ValueTypeInteger.ValueInteger> {

    public ValueTypeInteger() {
        super("integer", IModHelpers.get().getBaseHelpers().RGBToInt(243, 150, 4), ChatFormatting.GOLD, ValueTypeInteger.ValueInteger.class);
    }

    @Override
    public ValueInteger getDefault() {
        return ValueInteger.of(0);
    }

    @Override
    public MutableComponent toCompactString(ValueInteger value) {
        return Component.literal(Integer.toString(value.getRawValue()));
    }

    @Override
    public void serialize(ValueOutput valueOutput, ValueInteger value) {
        valueOutput.putInt("v", value.getRawValue());
    }

    @Override
    public ValueInteger deserialize(ValueInput valueInput) {
        return ValueInteger.of(valueInput.getInt("v").orElseThrow());
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
            throw new EvaluationException(Component.translatable(L10NValues.OPERATOR_ERROR_PARSE, value,
                    Component.translatable(getTranslationKey())));
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
    public ValueTypeString.ValueString compact(ValueInteger a) {
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
    public String getName(ValueInteger a) {
        return toCompactString(a).getString();
    }

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

        @Override
        public String toString() {
            return "ValueInteger(value=" + this.value + ")";
        }
    }

}
