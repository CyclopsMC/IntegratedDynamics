package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;

import java.util.Optional;

/**
 * An NBT int array wrapper
 */
public class ValueTypeListProxyNbtValueListLong extends ValueTypeListProxyNbtValueListGeneric<LongArrayTag, ValueTypeLong, ValueTypeLong.ValueLong> {

    public ValueTypeListProxyNbtValueListLong(String key, Optional<Tag> tag) {
        super(ValueTypeListProxyFactories.NBT_VALUE_LIST_LONG.getName(), ValueTypes.LONG, key, tag);
    }

    @Override
    protected int getLength(LongArrayTag tag) {
        return tag.getAsLongArray().length;
    }

    @Override
    protected ValueTypeLong.ValueLong get(LongArrayTag tag, int index) {
        return ValueTypeLong.ValueLong.of(tag.getAsLongArray()[index]);
    }

    public static class Factory extends ValueTypeListProxyNbtValueListGeneric.Factory<ValueTypeListProxyNbtValueListLong, LongArrayTag, ValueTypeLong, ValueTypeLong.ValueLong> {

        @Override
        public ResourceLocation getName() {
            return new ResourceLocation(Reference.MOD_ID, "nbt.list_value_long");
        }

        @Override
        protected ValueTypeListProxyNbtValueListLong create(String key, Optional<Tag> tag) {
            return new ValueTypeListProxyNbtValueListLong(key, tag);
        }
    }
}
