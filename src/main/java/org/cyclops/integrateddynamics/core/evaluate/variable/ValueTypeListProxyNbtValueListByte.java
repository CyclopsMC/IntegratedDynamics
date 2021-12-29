package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;

import java.util.Optional;

/**
 * An NBT byte array wrapper
 */
public class ValueTypeListProxyNbtValueListByte extends ValueTypeListProxyNbtValueListGeneric<ByteArrayTag, ValueTypeInteger, ValueTypeInteger.ValueInteger> {

    public ValueTypeListProxyNbtValueListByte(String key, Optional<Tag> tag) {
        super(ValueTypeListProxyFactories.NBT_VALUE_LIST_BYTE.getName(), ValueTypes.INTEGER, key, tag);
    }

    @Override
    protected int getLength(ByteArrayTag tag) {
        return tag.getAsByteArray().length;
    }

    @Override
    protected ValueTypeInteger.ValueInteger get(ByteArrayTag tag, int index) {
        return ValueTypeInteger.ValueInteger.of(tag.getAsByteArray()[index]);
    }

    public static class Factory extends ValueTypeListProxyNbtValueListGeneric.Factory<ValueTypeListProxyNbtValueListByte, ByteArrayTag, ValueTypeInteger, ValueTypeInteger.ValueInteger> {

        @Override
        public ResourceLocation getName() {
            return new ResourceLocation(Reference.MOD_ID, "nbt.list_value_byte");
        }

        @Override
        protected ValueTypeListProxyNbtValueListByte create(String key, Optional<Tag> tag) {
            return new ValueTypeListProxyNbtValueListByte(key, tag);
        }
    }
}
