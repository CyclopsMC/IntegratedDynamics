package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.CompoundNBT;

/**
 * An NBT byte array wrapper
 */
public class ValueTypeListProxyNbtValueListByte extends ValueTypeListProxyNbtValueListGeneric<ByteArrayNBT, ValueTypeInteger, ValueTypeInteger.ValueInteger> {

    public ValueTypeListProxyNbtValueListByte(String key, CompoundNBT tag) {
        super(ValueTypeListProxyFactories.NBT_VALUE_LIST_BYTE.getName(), ValueTypes.INTEGER, key, tag);
    }

    @Override
    protected int getLength(ByteArrayNBT tag) {
        return tag.getByteArray().length;
    }

    @Override
    protected ValueTypeInteger.ValueInteger get(ByteArrayNBT tag, int index) {
        return ValueTypeInteger.ValueInteger.of(tag.getByteArray()[index]);
    }

    @Override
    protected ByteArrayNBT getDefault() {
        return new ByteArrayNBT(new byte[0]);
    }

    public static class Factory extends ValueTypeListProxyNbtValueListGeneric.Factory<ValueTypeListProxyNbtValueListByte, ByteArrayNBT, ValueTypeInteger, ValueTypeInteger.ValueInteger> {

        @Override
        public String getName() {
            return "nbt.listValueByte";
        }

        @Override
        protected ValueTypeListProxyNbtValueListByte create(String key, CompoundNBT tag) {
            return new ValueTypeListProxyNbtValueListByte(key, tag);
        }
    }
}
