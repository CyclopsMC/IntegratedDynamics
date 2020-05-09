package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.util.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;

import java.util.Optional;

/**
 * An NBT long array wrapper
 */
public class ValueTypeListProxyNbtAsListLong extends ValueTypeListProxyNbtAsListGeneric<LongArrayNBT, ValueTypeLong, ValueTypeLong.ValueLong> {

    public ValueTypeListProxyNbtAsListLong(Optional<INBT> tag) {
        super(ValueTypeListProxyFactories.NBT_AS_LIST_LONG.getName(), ValueTypes.LONG, tag);
    }

    @Override
    protected int getLength(LongArrayNBT tag) {
        return tag.getAsLongArray().length;
    }

    @Override
    protected ValueTypeLong.ValueLong get(LongArrayNBT tag, int index) {
        return ValueTypeLong.ValueLong.of(tag.getAsLongArray()[index]);
    }

    public static class Factory extends ValueTypeListProxyNbtAsListGeneric.Factory<ValueTypeListProxyNbtAsListLong, LongArrayNBT, ValueTypeLong, ValueTypeLong.ValueLong> {

        @Override
        public ResourceLocation getName() {
            return new ResourceLocation(Reference.MOD_ID, "nbt.list_as_value_long");
        }

        @Override
        protected ValueTypeListProxyNbtAsListLong create(Optional<INBT> tag) {
            return new ValueTypeListProxyNbtAsListLong(tag);
        }
    }
}
