package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.util.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;

import java.util.Optional;

/**
 * An NBT int array wrapper
 */
public class ValueTypeListProxyNbtValueListInt extends ValueTypeListProxyNbtValueListGeneric<IntArrayNBT, ValueTypeInteger, ValueTypeInteger.ValueInteger> {

    public ValueTypeListProxyNbtValueListInt(String key, Optional<INBT> tag) {
        super(ValueTypeListProxyFactories.NBT_VALUE_LIST_INT.getName(), ValueTypes.INTEGER, key, tag);
    }

    @Override
    protected int getLength(IntArrayNBT tag) {
        return tag.getIntArray().length;
    }

    @Override
    protected ValueTypeInteger.ValueInteger get(IntArrayNBT tag, int index) {
        return ValueTypeInteger.ValueInteger.of(tag.getIntArray()[index]);
    }

    public static class Factory extends ValueTypeListProxyNbtValueListGeneric.Factory<ValueTypeListProxyNbtValueListInt, IntArrayNBT, ValueTypeInteger, ValueTypeInteger.ValueInteger> {

        @Override
        public ResourceLocation getName() {
            return new ResourceLocation(Reference.MOD_ID, "nbt.list_value_int");
        }

        @Override
        protected ValueTypeListProxyNbtValueListInt create(String key, Optional<INBT> tag) {
            return new ValueTypeListProxyNbtValueListInt(key, tag);
        }
    }
}
