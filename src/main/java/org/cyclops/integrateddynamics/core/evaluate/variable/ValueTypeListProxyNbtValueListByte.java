package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;

import java.util.Optional;

/**
 * An NBT byte array wrapper
 */
public class ValueTypeListProxyNbtValueListByte extends ValueTypeListProxyNbtValueListGeneric<ByteArrayNBT, ValueTypeInteger, ValueTypeInteger.ValueInteger> {

    public ValueTypeListProxyNbtValueListByte(String key, Optional<INBT> tag) {
        super(ValueTypeListProxyFactories.NBT_VALUE_LIST_BYTE.getName(), ValueTypes.INTEGER, key, tag);
    }

    @Override
    protected int getLength(ByteArrayNBT tag) {
        return tag.getAsByteArray().length;
    }

    @Override
    protected ValueTypeInteger.ValueInteger get(ByteArrayNBT tag, int index) {
        return ValueTypeInteger.ValueInteger.of(tag.getAsByteArray()[index]);
    }

    public static class Factory extends ValueTypeListProxyNbtValueListGeneric.Factory<ValueTypeListProxyNbtValueListByte, ByteArrayNBT, ValueTypeInteger, ValueTypeInteger.ValueInteger> {

        @Override
        public ResourceLocation getName() {
            return new ResourceLocation(Reference.MOD_ID, "nbt.list_value_byte");
        }

        @Override
        protected ValueTypeListProxyNbtValueListByte create(String key, Optional<INBT> tag) {
            return new ValueTypeListProxyNbtValueListByte(key, tag);
        }
    }
}
