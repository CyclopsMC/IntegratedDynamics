package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;

import java.util.Optional;

/**
 * An NBT tag list wrapper
 */
public class ValueTypeListProxyNbtAsListTag extends ValueTypeListProxyNbtAsListGeneric<ListNBT, ValueTypeNbt, ValueTypeNbt.ValueNbt> {

    public ValueTypeListProxyNbtAsListTag(Optional<INBT> tag) {
        super(ValueTypeListProxyFactories.NBT_AS_LIST_TAG.getName(), ValueTypes.NBT, tag);
    }

    @Override
    protected int getLength(ListNBT tag) {
        return tag.size();
    }

    @Override
    protected ValueTypeNbt.ValueNbt get(ListNBT tag, int index) {
        return ValueTypeNbt.ValueNbt.of(tag.get(index));
    }

    public static class Factory extends ValueTypeListProxyNbtAsListGeneric.Factory<ValueTypeListProxyNbtAsListTag, ListNBT, ValueTypeNbt, ValueTypeNbt.ValueNbt> {

        @Override
        public ResourceLocation getName() {
            return new ResourceLocation(Reference.MOD_ID, "nbt.list_as_value_tag");
        }

        @Override
        protected ValueTypeListProxyNbtAsListTag create(Optional<INBT> tag) {
            return new ValueTypeListProxyNbtAsListTag(tag);
        }
    }
}
