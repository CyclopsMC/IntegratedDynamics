package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;

import java.util.Optional;

/**
 * A list of NBT tags.
 */
public class ValueTypeListProxyNbtValueListTag extends ValueTypeListProxyNbtValueListGeneric<ListNBT, ValueTypeNbt, ValueTypeNbt.ValueNbt> {

    public ValueTypeListProxyNbtValueListTag(String key, Optional<INBT> tag) {
        super(ValueTypeListProxyFactories.NBT_VALUE_LIST_TAG.getName(), ValueTypes.NBT, key, tag);
    }

    @Override
    protected int getLength(ListNBT tag) {
        return tag.size();
    }

    @Override
    protected ValueTypeNbt.ValueNbt get(ListNBT tag, int index) {
        return ValueTypeNbt.ValueNbt.of(tag.get(index));
    }

    public static class Factory extends ValueTypeListProxyNbtValueListGeneric.Factory<ValueTypeListProxyNbtValueListTag, ListNBT, ValueTypeNbt, ValueTypeNbt.ValueNbt> {

        @Override
        public ResourceLocation getName() {
            return new ResourceLocation(Reference.MOD_ID, "nbt.list_value_tag");
        }

        @Override
        protected ValueTypeListProxyNbtValueListTag create(String key, Optional<INBT> tag) {
            return new ValueTypeListProxyNbtValueListTag(key, tag);
        }
    }
}
