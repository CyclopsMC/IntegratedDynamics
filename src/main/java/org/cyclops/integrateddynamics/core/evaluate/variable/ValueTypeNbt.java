package org.cyclops.integrateddynamics.core.evaluate.variable;

import lombok.ToString;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;

import javax.annotation.Nullable;

/**
 * Value type with values that are NBT tags.
 * @author rubensworks
 */
public class ValueTypeNbt extends ValueTypeBase<ValueTypeNbt.ValueNbt> implements IValueTypeNullable<ValueTypeNbt.ValueNbt> {

    public ValueTypeNbt() {
        super("nbt", Helpers.RGBToInt(0, 170, 170), TextFormatting.DARK_AQUA.toString());
    }

    @Override
    public ValueNbt getDefault() {
        return ValueNbt.of(new NBTTagCompound());
    }

    @Override
    public String toCompactString(ValueNbt value) {
        return value.getRawValue().toString();
    }

    @Override
    public String serialize(ValueNbt value) {
        return isNull(value) ? "{}" : value.getRawValue().toString();
    }

    @Override
    public ValueNbt deserialize(String value) {
        try {
            return ValueNbt.of(JsonToNBT.getTagFromJson(value));
        } catch (NBTException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public boolean isNull(ValueNbt a) {
        return a.getRawValue() instanceof NBTTagCompound && ((NBTTagCompound) a.getRawValue()).getSize() == 0;
    }

    @ToString
    public static class ValueNbt extends ValueBase {

        private final NBTTagCompound value;

        private ValueNbt(NBTTagCompound value) {
            super(ValueTypes.NBT);
            this.value = value;
        }

        public static ValueNbt of(@Nullable NBTTagCompound value) {
            return value == null ? ValueTypes.NBT.getDefault() : new ValueNbt(value);
        }

        public NBTTagCompound getRawValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ValueNbt && ((ValueNbt) o).value.equals(this.value);
        }

        @Override
        public int hashCode() {
            return getType().hashCode() + value.hashCode();
        }
    }

}
