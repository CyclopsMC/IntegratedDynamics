package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.ListNBT;

/**
 * A list of NBT tags.
 */
public class ValueTypeListProxyNbtValueListTag extends ValueTypeListProxyNbtValueListGeneric<ListNBT, ValueTypeNbt, ValueTypeNbt.ValueNbt> {

    public ValueTypeListProxyNbtValueListTag(String key, CompoundNBT tag) {
        super(ValueTypeListProxyFactories.NBT_VALUE_LIST_TAG.getName(), ValueTypes.NBT, key, tag);
    }

    @Override
    protected int getLength(ListNBT tag) {
        return tag.size();
    }

    @Override
    protected ValueTypeNbt.ValueNbt get(ListNBT tag, int index) {
        return ValueTypeNbt.ValueNbt.of(tag.getCompound(index));
    }

    @Override
    protected ListNBT getDefault() {
        return new ListNBT();
    }

    public static class Factory extends ValueTypeListProxyNbtValueListGeneric.Factory<ValueTypeListProxyNbtValueListTag, ListNBT, ValueTypeNbt, ValueTypeNbt.ValueNbt> {

        @Override
        public String getName() {
            return "nbt.listValueTag";
        }

        @Override
        protected ValueTypeListProxyNbtValueListTag create(String key, CompoundNBT tag) {
            return new ValueTypeListProxyNbtValueListTag(key, tag);
        }
    }
}
