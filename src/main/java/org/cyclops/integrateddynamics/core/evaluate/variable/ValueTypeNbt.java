package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.ToString;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;

import javax.annotation.Nullable;

/**
 * Value type with values that are NBT tags.
 * @author rubensworks
 */
public class ValueTypeNbt extends ValueTypeBase<ValueTypeNbt.ValueNbt>
        implements IValueTypeNullable<ValueTypeNbt.ValueNbt>, IValueTypeNamed<ValueTypeNbt.ValueNbt> {

    public ValueTypeNbt() {
        super("nbt", Helpers.RGBToInt(0, 170, 170), TextFormatting.DARK_AQUA);
    }

    @Override
    public ValueNbt getDefault() {
        return ValueNbt.of(new CompoundNBT());
    }

    @Override
    public ITextComponent toCompactString(ValueNbt value) {
        return new StringTextComponent(value.getRawValue().toString());
    }

    @Override
    public INBT serialize(ValueNbt value) {
        return value.getRawValue();
    }

    @Override
    public ValueNbt deserialize(INBT value) {
        try {
            return ValueNbt.of((CompoundNBT) value);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public String toString(ValueNbt value) {
        return isNull(value) ? "{}" : value.getRawValue().toString();
    }

    @Override
    public ValueNbt parseString(String value) throws EvaluationException {
        try {
            return ValueNbt.of(JsonToNBT.getTagFromJson(value));
        } catch (CommandSyntaxException e) {
            throw new EvaluationException(e.getMessage());
        }
    }

    @Override
    public boolean isNull(ValueNbt a) {
        return a.getRawValue().size() == 0;
    }

    /**
     * Filter away the blacklisted tags from the given NBT tag.
     * This won't modify the original tag.
     * @param tag The tag.
     * @return The tag where all blacklisted tags have been removed.
     */
    public CompoundNBT filterBlacklistedTags(CompoundNBT tag) {
        boolean copied = false;
        for (String key : GeneralConfig.nbtTagBlacklist) {
            if (tag.contains(key)) {
                if (!copied) {
                    copied = true;
                    tag = tag.copy();
                }
                tag.remove(key);
            }
        }
        return tag;
    }

    @Override
    public String getName(ValueNbt value) {
        return toCompactString(value).getString();
    }

    @ToString
    public static class ValueNbt extends ValueBase {

        private final CompoundNBT value;

        private ValueNbt(CompoundNBT value) {
            super(ValueTypes.NBT);
            this.value = ValueTypes.NBT.filterBlacklistedTags(value);
        }

        public static ValueNbt of(@Nullable CompoundNBT value) {
            return value == null ? ValueTypes.NBT.getDefault() : new ValueNbt(value);
        }

        public CompoundNBT getRawValue() {
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
