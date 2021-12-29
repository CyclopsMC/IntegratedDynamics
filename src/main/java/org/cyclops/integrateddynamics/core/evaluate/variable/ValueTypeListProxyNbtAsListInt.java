package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;

import java.util.Optional;

/**
 * An NBT int array wrapper
 */
public class ValueTypeListProxyNbtAsListInt extends ValueTypeListProxyNbtAsListGeneric<IntArrayTag, ValueTypeInteger, ValueTypeInteger.ValueInteger> {

    public ValueTypeListProxyNbtAsListInt(Optional<Tag> tag) {
        super(ValueTypeListProxyFactories.NBT_AS_LIST_INT.getName(), ValueTypes.INTEGER, tag);
    }

    @Override
    protected int getLength(IntArrayTag tag) {
        return tag.getAsIntArray().length;
    }

    @Override
    protected ValueTypeInteger.ValueInteger get(IntArrayTag tag, int index) {
        return ValueTypeInteger.ValueInteger.of(tag.getAsIntArray()[index]);
    }

    public static class Factory extends ValueTypeListProxyNbtAsListGeneric.Factory<ValueTypeListProxyNbtAsListInt, IntArrayTag, ValueTypeInteger, ValueTypeInteger.ValueInteger> {

        @Override
        public ResourceLocation getName() {
            return new ResourceLocation(Reference.MOD_ID, "nbt.list_as_value_int");
        }

        @Override
        protected ValueTypeListProxyNbtAsListInt create(Optional<Tag> tag) {
            return new ValueTypeListProxyNbtAsListInt(tag);
        }
    }
}
