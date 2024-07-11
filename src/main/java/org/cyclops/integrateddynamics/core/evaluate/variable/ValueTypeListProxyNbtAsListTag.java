package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;

import java.util.Optional;

/**
 * An NBT tag list wrapper
 */
public class ValueTypeListProxyNbtAsListTag extends ValueTypeListProxyNbtAsListGeneric<ListTag, ValueTypeNbt, ValueTypeNbt.ValueNbt> {

    public ValueTypeListProxyNbtAsListTag(Optional<Tag> tag) {
        super(ValueTypeListProxyFactories.NBT_AS_LIST_TAG.getName(), ValueTypes.NBT, tag);
    }

    @Override
    protected int getLength(ListTag tag) {
        return tag.size();
    }

    @Override
    protected ValueTypeNbt.ValueNbt get(ListTag tag, int index) {
        return ValueTypeNbt.ValueNbt.of(tag.get(index));
    }

    public static class Factory extends ValueTypeListProxyNbtAsListGeneric.Factory<ValueTypeListProxyNbtAsListTag, ListTag, ValueTypeNbt, ValueTypeNbt.ValueNbt> {

        @Override
        public ResourceLocation getName() {
            return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "nbt.list_as_value_tag");
        }

        @Override
        protected ValueTypeListProxyNbtAsListTag create(Optional<Tag> tag) {
            return new ValueTypeListProxyNbtAsListTag(tag);
        }
    }
}
