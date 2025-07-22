package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNamed;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNullable;
import org.cyclops.integrateddynamics.core.helper.Helpers;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Value type with values that are NBT tags.
 * @author rubensworks
 */
public class ValueTypeNbt extends ValueTypeBase<ValueTypeNbt.ValueNbt>
        implements IValueTypeNullable<ValueTypeNbt.ValueNbt>, IValueTypeNamed<ValueTypeNbt.ValueNbt> {

    public ValueTypeNbt() {
        super("nbt", IModHelpers.get().getBaseHelpers().RGBToInt(0, 170, 170), ChatFormatting.DARK_AQUA, ValueTypeNbt.ValueNbt.class);
    }

    @Override
    public ValueNbt getDefault() {
        return ValueNbt.of();
    }

    @Override
    public MutableComponent toCompactString(ValueNbt value) {
        return Component.literal(toString(value));
    }

    @Override
    public void serialize(ValueOutput valueOutput, ValueNbt value) {
        if (value.getRawValue().isPresent()) {
            valueOutput.store("v", ExtraCodecs.NBT, value.getRawValue().get());
        }
    }

    @Override
    public ValueNbt deserialize(ValueInput valueInput) {
        return ValueNbt.of(valueInput.read("v", ExtraCodecs.NBT));
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
            return ValueNbt.of(Helpers.TAG_PARSER.parseFully(value));
        } catch (CommandSyntaxException e) {
            throw new EvaluationException(Component.literal(e.getMessage()));
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

        @Override
        public String toString() {
            return getRawValue().map(Tag::toString).orElse("none");
        }
    }

}
