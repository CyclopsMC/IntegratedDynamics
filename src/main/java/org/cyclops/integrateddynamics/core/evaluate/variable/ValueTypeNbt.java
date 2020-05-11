package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.mojang.brigadier.StringReader;
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
import java.util.Optional;

/**
 * Value type with values that are NBT tags.
 * @author rubensworks
 */
public class ValueTypeNbt extends ValueTypeBase<ValueTypeNbt.ValueNbt>
        implements IValueTypeNullable<ValueTypeNbt.ValueNbt>, IValueTypeNamed<ValueTypeNbt.ValueNbt> {

    public ValueTypeNbt() {
        super("nbt", Helpers.RGBToInt(0, 170, 170), TextFormatting.DARK_AQUA, ValueTypeNbt.ValueNbt.class);
    }

    @Override
    public ValueNbt getDefault() {
        return ValueNbt.of();
    }

    @Override
    public ITextComponent toCompactString(ValueNbt value) {
        return new StringTextComponent(toString(value));
    }

    @Override
    public INBT serialize(ValueNbt value) {
        CompoundNBT tag = new CompoundNBT();
        if (value.getRawValue().isPresent()) {
            tag.put("v", value.getRawValue().get());
        }
        return tag;
    }

    @Override
    public ValueNbt deserialize(INBT value) {
        if (value instanceof CompoundNBT && ((CompoundNBT) value).contains("v")) {
            return ValueNbt.of(((CompoundNBT) value).get("v"));
        }
        return ValueNbt.of();
    }

    @Override
    public String toString(ValueNbt value) {
        return value.getRawValue().map(Object::toString).orElse("");
    }

    @Override
    public ValueNbt parseString(String value) throws EvaluationException {
        if (value.isEmpty()) {
            return ValueNbt.of();
        }
        try {
            return ValueNbt.of(new JsonToNBT(new StringReader(value)).readValue());
        } catch (CommandSyntaxException e) {
            throw new EvaluationException(new StringTextComponent(e.getMessage()));
        }
    }

    @Override
    public boolean isNull(ValueNbt a) {
        return !a.getRawValue().isPresent();
    }

    /**
     * Filter away the blacklisted tags from the given NBT tag.
     * This won't modify the original tag.
     * @param tag The tag.
     * @return The tag where all blacklisted tags have been removed.
     */
    public INBT filterBlacklistedTags(INBT tag) {
        if (tag instanceof CompoundNBT) {
            boolean copied = false;
            CompoundNBT compountTag = (CompoundNBT) tag;
            for (String key : GeneralConfig.nbtTagBlacklist) {
                if (compountTag.contains(key)) {
                    if (!copied) {
                        copied = true;
                        compountTag = compountTag.copy();
                    }
                    compountTag.remove(key);
                }
            }
            return compountTag;
        }
        return tag;
    }

    @Override
    public String getName(ValueNbt value) {
        return toCompactString(value).getString();
    }

    @ToString
    public static class ValueNbt extends ValueOptionalBase<INBT> {

        private ValueNbt(INBT value) {
            super(ValueTypes.NBT, value);
        }

        @Nullable
        @Override
        protected INBT preprocessValue(@Nullable INBT value) {
            if (value != null) {
                return ValueTypes.NBT.filterBlacklistedTags(value);
            }
            return null;
        }

        public static ValueNbt of(@Nullable INBT value) {
            return new ValueNbt(value);
        }

        public static ValueNbt of(Optional<INBT> value) {
            return of(value.orElse(null));
        }

        public static ValueNbt of() {
            return of((INBT) null);
        }

        @Override
        protected boolean isEqual(INBT a, INBT b) {
            return a.equals(b);
        }
    }

}
