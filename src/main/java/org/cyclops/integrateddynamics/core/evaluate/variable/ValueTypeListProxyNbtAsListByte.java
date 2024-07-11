package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;

import java.util.Optional;

/**
 * An NBT byte array wrapper
 */
public class ValueTypeListProxyNbtAsListByte extends ValueTypeListProxyNbtAsListGeneric<ByteArrayTag, ValueTypeInteger, ValueTypeInteger.ValueInteger> {

    public ValueTypeListProxyNbtAsListByte(Optional<Tag> tag) {
        super(ValueTypeListProxyFactories.NBT_AS_LIST_BYTE.getName(), ValueTypes.INTEGER, tag);
    }

    @Override
    protected int getLength(ByteArrayTag tag) {
        return tag.getAsByteArray().length;
    }

    @Override
    protected ValueTypeInteger.ValueInteger get(ByteArrayTag tag, int index) {
        return ValueTypeInteger.ValueInteger.of(tag.getAsByteArray()[index]);
    }

    public static class Factory extends ValueTypeListProxyNbtAsListGeneric.Factory<ValueTypeListProxyNbtAsListByte, ByteArrayTag, ValueTypeInteger, ValueTypeInteger.ValueInteger> {

        @Override
        public ResourceLocation getName() {
            return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "nbt.list_as_value_byte");
        }

        @Override
        protected ValueTypeListProxyNbtAsListByte create(Optional<Tag> tag) {
            return new ValueTypeListProxyNbtAsListByte(tag);
        }
    }
}
