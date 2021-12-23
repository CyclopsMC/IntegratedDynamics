package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.util.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;

import java.util.Optional;

/**
 * An NBT int array wrapper
 */
public class ValueTypeListProxyNbtAsListInt extends ValueTypeListProxyNbtAsListGeneric<IntArrayNBT, ValueTypeInteger, ValueTypeInteger.ValueInteger> {

    public ValueTypeListProxyNbtAsListInt(Optional<INBT> tag) {
        super(ValueTypeListProxyFactories.NBT_AS_LIST_INT.getName(), ValueTypes.INTEGER, tag);
    }

    @Override
    protected int getLength(IntArrayNBT tag) {
        return tag.getAsIntArray().length;
    }

    @Override
    protected ValueTypeInteger.ValueInteger get(IntArrayNBT tag, int index) {
        return ValueTypeInteger.ValueInteger.of(tag.getAsIntArray()[index]);
    }

    public static class Factory extends ValueTypeListProxyNbtAsListGeneric.Factory<ValueTypeListProxyNbtAsListInt, IntArrayNBT, ValueTypeInteger, ValueTypeInteger.ValueInteger> {

        @Override
        public ResourceLocation getName() {
            return new ResourceLocation(Reference.MOD_ID, "nbt.list_as_value_int");
        }

        @Override
        protected ValueTypeListProxyNbtAsListInt create(Optional<INBT> tag) {
            return new ValueTypeListProxyNbtAsListInt(tag);
        }
    }
}
