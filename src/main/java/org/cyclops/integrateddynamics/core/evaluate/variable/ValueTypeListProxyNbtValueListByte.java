package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;

/**
 * An NBT byte array wrapper
 */
public class ValueTypeListProxyNbtValueListByte extends ValueTypeListProxyNbtValueListGeneric<NBTTagByteArray, ValueTypeInteger, ValueTypeInteger.ValueInteger> {

    public ValueTypeListProxyNbtValueListByte(String key, NBTTagCompound tag) {
        super(ValueTypeListProxyFactories.NBT_VALUE_LIST_BYTE.getName(), ValueTypes.INTEGER, key, tag);
    }

    @Override
    protected int getLength(NBTTagByteArray tag) {
        return tag.getByteArray().length;
    }

    @Override
    protected ValueTypeInteger.ValueInteger get(NBTTagByteArray tag, int index) {
        return ValueTypeInteger.ValueInteger.of(tag.getByteArray()[index]);
    }

    @Override
    protected NBTTagByteArray getDefault() {
        return new NBTTagByteArray(new byte[0]);
    }

    public static class Factory extends ValueTypeListProxyNbtValueListGeneric.Factory<ValueTypeListProxyNbtValueListByte, NBTTagByteArray, ValueTypeInteger, ValueTypeInteger.ValueInteger> {

        @Override
        public String getName() {
            return "nbt.listValueByte";
        }

        @Override
        protected ValueTypeListProxyNbtValueListByte create(String key, NBTTagCompound tag) {
            return new ValueTypeListProxyNbtValueListByte(key, tag);
        }
    }
}
