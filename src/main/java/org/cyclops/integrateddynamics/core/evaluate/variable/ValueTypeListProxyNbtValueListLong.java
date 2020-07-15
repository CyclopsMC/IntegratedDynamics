package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.util.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;

import java.util.Optional;

/**
 * An NBT int array wrapper
 */
public class ValueTypeListProxyNbtValueListLong extends ValueTypeListProxyNbtValueListGeneric<LongArrayNBT, ValueTypeLong, ValueTypeLong.ValueLong> {

    public ValueTypeListProxyNbtValueListLong(String key, Optional<INBT> tag) {
        super(ValueTypeListProxyFactories.NBT_VALUE_LIST_LONG.getName(), ValueTypes.LONG, key, tag);
    }

    @Override
    protected int getLength(LongArrayNBT tag) {
        return tag.getAsLongArray().length;
    }

    @Override
    protected ValueTypeLong.ValueLong get(LongArrayNBT tag, int index) {
        return ValueTypeLong.ValueLong.of(tag.getAsLongArray()[index]);
    }

    public static class Factory extends ValueTypeListProxyNbtValueListGeneric.Factory<ValueTypeListProxyNbtValueListLong, LongArrayNBT, ValueTypeLong, ValueTypeLong.ValueLong> {

        @Override
        public ResourceLocation getName() {
            return new ResourceLocation(Reference.MOD_ID, "nbt.list_value_long");
        }

        @Override
        protected ValueTypeListProxyNbtValueListLong create(String key, Optional<INBT> tag) {
            return new ValueTypeListProxyNbtValueListLong(key, tag);
        }
    }
}
