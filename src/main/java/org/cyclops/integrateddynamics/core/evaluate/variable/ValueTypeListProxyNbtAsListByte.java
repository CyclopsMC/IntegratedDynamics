package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;

import java.util.Optional;

/**
 * An NBT byte array wrapper
 */
public class ValueTypeListProxyNbtAsListByte extends ValueTypeListProxyNbtAsListGeneric<ByteArrayNBT, ValueTypeInteger, ValueTypeInteger.ValueInteger> {

    public ValueTypeListProxyNbtAsListByte(Optional<INBT> tag) {
        super(ValueTypeListProxyFactories.NBT_AS_LIST_BYTE.getName(), ValueTypes.INTEGER, tag);
    }

    @Override
    protected int getLength(ByteArrayNBT tag) {
        return tag.getByteArray().length;
    }

    @Override
    protected ValueTypeInteger.ValueInteger get(ByteArrayNBT tag, int index) {
        return ValueTypeInteger.ValueInteger.of(tag.getByteArray()[index]);
    }

    public static class Factory extends ValueTypeListProxyNbtAsListGeneric.Factory<ValueTypeListProxyNbtAsListByte, ByteArrayNBT, ValueTypeInteger, ValueTypeInteger.ValueInteger> {

        @Override
        public ResourceLocation getName() {
            return new ResourceLocation(Reference.MOD_ID, "nbt.list_as_value_byte");
        }

        @Override
        protected ValueTypeListProxyNbtAsListByte create(Optional<INBT> tag) {
            return new ValueTypeListProxyNbtAsListByte(tag);
        }
    }
}
