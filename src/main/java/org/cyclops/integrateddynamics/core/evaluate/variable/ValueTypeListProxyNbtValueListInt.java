package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;

import java.util.Optional;

/**
 * An NBT int array wrapper
 */
public class ValueTypeListProxyNbtValueListInt extends ValueTypeListProxyNbtValueListGeneric<IntArrayTag, ValueTypeInteger, ValueTypeInteger.ValueInteger> {

    public ValueTypeListProxyNbtValueListInt(String key, Optional<Tag> tag) {
        super(ValueTypeListProxyFactories.NBT_VALUE_LIST_INT.getName(), ValueTypes.INTEGER, key, tag);
    }

    @Override
    protected int getLength(IntArrayTag tag) {
        return tag.getAsIntArray().length;
    }

    @Override
    protected ValueTypeInteger.ValueInteger get(IntArrayTag tag, int index) {
        return ValueTypeInteger.ValueInteger.of(tag.getAsIntArray()[index]);
    }

    public static class Factory extends ValueTypeListProxyNbtValueListGeneric.Factory<ValueTypeListProxyNbtValueListInt, IntArrayTag, ValueTypeInteger, ValueTypeInteger.ValueInteger> {

        @Override
        public ResourceLocation getName() {
            return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "nbt.list_value_int");
        }

        @Override
        protected ValueTypeListProxyNbtValueListInt create(String key, Optional<Tag> tag) {
            return new ValueTypeListProxyNbtValueListInt(key, tag);
        }
    }
}
