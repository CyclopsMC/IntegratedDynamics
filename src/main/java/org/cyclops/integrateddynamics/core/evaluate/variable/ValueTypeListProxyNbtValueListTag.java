package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/**
 * A list of NBT tags.
 */
public class ValueTypeListProxyNbtValueListTag extends ValueTypeListProxyNbtValueListGeneric<NBTTagList, ValueTypeNbt, ValueTypeNbt.ValueNbt> {

    public ValueTypeListProxyNbtValueListTag(String key, NBTTagCompound tag) {
        super(ValueTypeListProxyFactories.NBT_VALUE_LIST_TAG.getName(), ValueTypes.NBT, key, tag);
    }

    @Override
    protected int getLength(NBTTagList tag) {
        return tag.tagCount();
    }

    @Override
    protected ValueTypeNbt.ValueNbt get(NBTTagList tag, int index) {
        return ValueTypeNbt.ValueNbt.of(tag.getCompoundTagAt(index));
    }

    @Override
    protected NBTTagList getDefault() {
        return new NBTTagList();
    }

    public static class Factory extends ValueTypeListProxyNbtValueListGeneric.Factory<ValueTypeListProxyNbtValueListTag, NBTTagList, ValueTypeNbt, ValueTypeNbt.ValueNbt> {

        @Override
        public String getName() {
            return "nbt.listValueTag";
        }

        @Override
        protected ValueTypeListProxyNbtValueListTag create(String key, NBTTagCompound tag) {
            return new ValueTypeListProxyNbtValueListTag(key, tag);
        }
    }
}
