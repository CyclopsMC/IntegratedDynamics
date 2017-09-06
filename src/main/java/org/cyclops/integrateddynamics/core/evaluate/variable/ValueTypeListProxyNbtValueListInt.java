package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;

/**
 * An NBT int array wrapper
 */
public class ValueTypeListProxyNbtValueListInt extends ValueTypeListProxyNbtValueListGeneric<NBTTagIntArray, ValueTypeInteger, ValueTypeInteger.ValueInteger> {

    public ValueTypeListProxyNbtValueListInt(String key, NBTTagCompound tag) {
        super(ValueTypeListProxyFactories.NBT_VALUE_LIST_INT.getName(), ValueTypes.INTEGER, key, tag);
    }

    @Override
    protected int getLength(NBTTagIntArray tag) {
        return tag.getIntArray().length;
    }

    @Override
    protected ValueTypeInteger.ValueInteger get(NBTTagIntArray tag, int index) {
        return ValueTypeInteger.ValueInteger.of(tag.getIntArray()[index]);
    }

    @Override
    protected NBTTagIntArray getDefault() {
        return new NBTTagIntArray(new int[0]);
    }

    public static class Factory extends ValueTypeListProxyNbtValueListGeneric.Factory<ValueTypeListProxyNbtValueListInt, NBTTagIntArray, ValueTypeInteger, ValueTypeInteger.ValueInteger> {

        @Override
        public String getName() {
            return "nbt.listValueInt";
        }

        @Override
        protected ValueTypeListProxyNbtValueListInt create(String key, NBTTagCompound tag) {
            return new ValueTypeListProxyNbtValueListInt(key, tag);
        }
    }
}
