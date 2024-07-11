package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;

import java.util.Optional;

/**
 * An NBT long array wrapper
 */
public class ValueTypeListProxyNbtAsListLong extends ValueTypeListProxyNbtAsListGeneric<LongArrayTag, ValueTypeLong, ValueTypeLong.ValueLong> {

    public ValueTypeListProxyNbtAsListLong(Optional<Tag> tag) {
        super(ValueTypeListProxyFactories.NBT_AS_LIST_LONG.getName(), ValueTypes.LONG, tag);
    }

    @Override
    protected int getLength(LongArrayTag tag) {
        return tag.getAsLongArray().length;
    }

    @Override
    protected ValueTypeLong.ValueLong get(LongArrayTag tag, int index) {
        return ValueTypeLong.ValueLong.of(tag.getAsLongArray()[index]);
    }

    public static class Factory extends ValueTypeListProxyNbtAsListGeneric.Factory<ValueTypeListProxyNbtAsListLong, LongArrayTag, ValueTypeLong, ValueTypeLong.ValueLong> {

        @Override
        public ResourceLocation getName() {
            return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "nbt.list_as_value_long");
        }

        @Override
        protected ValueTypeListProxyNbtAsListLong create(Optional<Tag> tag) {
            return new ValueTypeListProxyNbtAsListLong(tag);
        }
    }
}
