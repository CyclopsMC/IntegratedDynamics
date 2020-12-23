package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

/**
 * Value type with values 'true' or 'false'
 * @author rubensworks
 */
public class ValueTypeBoolean extends ValueTypeBase<ValueTypeBoolean.ValueBoolean> {

    public ValueTypeBoolean() {
        super("boolean", Helpers.RGBToInt(43, 47, 231), TextFormatting.BLUE, ValueTypeBoolean.ValueBoolean.class);
    }

    @Override
    public ValueBoolean getDefault() {
        return ValueBoolean.of(false);
    }

    @Override
    public IFormattableTextComponent toCompactString(ValueBoolean value) {
        return new StringTextComponent(Boolean.toString(value.getRawValue()));
    }

    @Override
    public INBT serialize(ValueBoolean value) {
        return ByteNBT.valueOf(value.getRawValue() ? (byte) 1 : (byte) 0);
    }

    @Override
    public ValueBoolean deserialize(INBT value) {
        if (value.getId() == Constants.NBT.TAG_BYTE) {
            return ValueBoolean.of(((ByteNBT) value).getByte() == 1);
        } else {
            throw new IllegalArgumentException(String.format("Value \"%s\" could not be parsed to a boolean.", value));
        }
    }

    @Override
    public String toString(ValueBoolean value) {
        return Boolean.toString(value.getRawValue());
    }

    @Override
    public ValueBoolean parseString(String value) throws EvaluationException {
        boolean b;
        if("true".equalsIgnoreCase(value) || "1".equals(value)) {
            b = true;
        } else if("false".equalsIgnoreCase(value) || "0".equals(value)) {
            b = false;
        } else {
            throw new EvaluationException(new TranslationTextComponent(L10NValues.OPERATOR_ERROR_PARSE, value,
                    new TranslationTextComponent(getTranslationKey())));
        }
        return ValueBoolean.of(b);
    }

    @ToString
    public static class ValueBoolean extends ValueBase {

        private static final ValueBoolean TRUE = new ValueBoolean(true);
        private static final ValueBoolean FALSE = new ValueBoolean(false);

        private final boolean value;

        private ValueBoolean(boolean value) {
            super(ValueTypes.BOOLEAN);
            this.value = value;
        }

        public static ValueBoolean of(boolean value) {
            return value ? TRUE : FALSE;
        }

        public boolean getRawValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ValueBoolean && ((ValueBoolean) o).value == this.value;
        }

        @Override
        public int hashCode() {
            return getType().hashCode() + (value ? 1 : 0);
        }
    }

}
