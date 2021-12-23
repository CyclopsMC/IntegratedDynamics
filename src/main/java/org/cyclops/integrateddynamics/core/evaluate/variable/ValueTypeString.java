package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;

/**
 * Value type with values that are strings.
 * @author rubensworks
 */
public class ValueTypeString extends ValueTypeBase<ValueTypeString.ValueString>
        implements IValueTypeNamed<ValueTypeString.ValueString> {

    public ValueTypeString() {
        super("string", Helpers.RGBToInt(250, 10, 13), TextFormatting.RED, ValueTypeString.ValueString.class);
    }

    @Override
    public ValueString getDefault() {
        return ValueString.of("");
    }

    @Override
    public IFormattableTextComponent toCompactString(ValueString value) {
        return new StringTextComponent(value.getRawValue());
    }

    @Override
    public INBT serialize(ValueString value) {
        return StringNBT.valueOf(value.getRawValue());
    }

    @Override
    public ValueString deserialize(INBT value) {
        return ValueString.of(value.getAsString());
    }

    @Override
    public String toString(ValueString value) {
        return value.getRawValue();
    }

    @Override
    public ValueString parseString(String value) throws EvaluationException {
        return ValueString.of(value);
    }

    @Override
    public String getName(ValueString a) {
        return a.getRawValue();
    }

    @ToString
    public static class ValueString extends ValueBase {

        private final String value;

        private ValueString(String value) {
            super(ValueTypes.STRING);
            this.value = value;
        }

        public static ValueString of(String value) {
            return new ValueString(value);
        }

        public String getRawValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ValueString && ((ValueString) o).value.equals(this.value);
        }

        @Override
        public int hashCode() {
            return getType().hashCode() + value.hashCode();
        }
    }

}
