package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.ToString;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
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
        super("nbt", Helpers.RGBToInt(0, 170, 170), ChatFormatting.DARK_AQUA, ValueTypeNbt.ValueNbt.class);
    }

    @Override
    public ValueNbt getDefault() {
        return ValueNbt.of();
    }

    @Override
    public MutableComponent toCompactString(ValueNbt value) {
        return new TextComponent(toString(value));
    }

    @Override
    public Tag serialize(ValueNbt value) {
        CompoundTag tag = new CompoundTag();
        if (value.getRawValue().isPresent()) {
            tag.put("v", value.getRawValue().get());
        }
        return tag;
    }

    @Override
    public ValueNbt deserialize(Tag value) {
        if (value instanceof CompoundTag && ((CompoundTag) value).contains("v")) {
            return ValueNbt.of(((CompoundTag) value).get("v"));
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
            return ValueNbt.of(new TagParser(new StringReader(value)).readValue());
        } catch (CommandSyntaxException e) {
            throw new EvaluationException(new TextComponent(e.getMessage()));
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
    public Tag filterBlacklistedTags(Tag tag) {
        if (tag instanceof CompoundTag) {
            boolean copied = false;
            CompoundTag compountTag = (CompoundTag) tag;
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
    public static class ValueNbt extends ValueOptionalBase<Tag> {

        private ValueNbt(Tag value) {
            super(ValueTypes.NBT, value);
        }

        @Nullable
        @Override
        protected Tag preprocessValue(@Nullable Tag value) {
            if (value != null) {
                return ValueTypes.NBT.filterBlacklistedTags(value);
            }
            return null;
        }

        public static ValueNbt of(@Nullable Tag value) {
            return new ValueNbt(value);
        }

        public static ValueNbt of(Optional<Tag> value) {
            return of(value.orElse(null));
        }

        public static ValueNbt of() {
            return of((Tag) null);
        }

        @Override
        protected boolean isEqual(Tag a, Tag b) {
            return a.equals(b);
        }
    }

}
