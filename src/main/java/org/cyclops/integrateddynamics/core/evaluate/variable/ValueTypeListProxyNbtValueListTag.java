package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;

import java.util.Optional;

/**
 * A list of NBT tags.
 */
public class ValueTypeListProxyNbtValueListTag extends ValueTypeListProxyNbtValueListGeneric<ListTag, ValueTypeNbt, ValueTypeNbt.ValueNbt> {

    public ValueTypeListProxyNbtValueListTag(String key, Optional<Tag> tag) {
        super(ValueTypeListProxyFactories.NBT_VALUE_LIST_TAG.getName(), ValueTypes.NBT, key, tag);
    }

    @Override
    protected int getLength(ListTag tag) {
        return tag.size();
    }

    @Override
    protected ValueTypeNbt.ValueNbt get(ListTag tag, int index) {
        return ValueTypeNbt.ValueNbt.of(tag.get(index));
    }

    public static class Factory extends ValueTypeListProxyNbtValueListGeneric.Factory<ValueTypeListProxyNbtValueListTag, ListTag, ValueTypeNbt, ValueTypeNbt.ValueNbt> {

        @Override
        public ResourceLocation getName() {
            return new ResourceLocation(Reference.MOD_ID, "nbt.list_value_tag");
        }

        @Override
        protected ValueTypeListProxyNbtValueListTag create(String key, Optional<Tag> tag) {
            return new ValueTypeListProxyNbtValueListTag(key, tag);
        }
    }
}
