package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeBooleanLPElement;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;

/**
 * Value type with values 'true' or 'false'
 * @author rubensworks
 */
public class ValueTypeBoolean extends ValueTypeBase<ValueTypeBoolean.ValueBoolean> {

    public ValueTypeBoolean() {
        super("boolean", Helpers.RGBToInt(43, 47, 231), ChatFormatting.BLUE, ValueTypeBoolean.ValueBoolean.class);
    }

    @Override
    public ValueBoolean getDefault() {
        return ValueBoolean.of(false);
    }

    @Override
    public MutableComponent toCompactString(ValueBoolean value) {
        return new TextComponent(Boolean.toString(value.getRawValue()));
    }

    @Override
    public Tag serialize(ValueBoolean value) {
        return ByteTag.valueOf(value.getRawValue() ? (byte) 1 : (byte) 0);
    }

    @Override
    public ValueBoolean deserialize(Tag value) {
        if (value.getId() == Tag.TAG_BYTE) {
            return ValueBoolean.of(((ByteTag) value).getAsByte() == 1);
        } else {
            throw new IllegalArgumentException(String.format("Value \"%s\" could not be parsed to a boolean.", value));
        }
    }

    @Override
    public String toString(ValueBoolean value) {
        return Boolean.toString(value.getRawValue());
    }

    @Override
    public ValueTypeLPElementBase createLogicProgrammerElement() {
        return new ValueTypeBooleanLPElement(this);
    }

    @Override
    public ValueBoolean parseString(String value) throws EvaluationException {
        boolean b;
        if("true".equalsIgnoreCase(value) || "1".equals(value)) {
            b = true;
        } else if("false".equalsIgnoreCase(value) || "0".equals(value)) {
            b = false;
        } else {
            throw new EvaluationException(new TranslatableComponent(L10NValues.OPERATOR_ERROR_PARSE, value,
                    new TranslatableComponent(getTranslationKey())));
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
