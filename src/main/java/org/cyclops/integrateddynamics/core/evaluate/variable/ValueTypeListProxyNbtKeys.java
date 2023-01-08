package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Iterables;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;

import java.util.Optional;

/**
 * A list of NBT keys.
 */
public class ValueTypeListProxyNbtKeys extends ValueTypeListProxyBase<ValueTypeString, ValueTypeString.ValueString> {

    private final Optional<Tag> tag;

    public ValueTypeListProxyNbtKeys(Optional<Tag> tag) {
        super(ValueTypeListProxyFactories.NBT_KEYS.getName(), ValueTypes.STRING);
        this.tag = tag;
    }

    @Override
    public int getLength() throws EvaluationException {
        return tag
                .map(t -> t instanceof CompoundTag ? ((CompoundTag) t).getAllKeys().size() : 0)
                .orElse(0);
    }

    @Override
    public ValueTypeString.ValueString get(int index) throws EvaluationException {
        if (index < getLength()) {
            return ValueTypeString.ValueString.of(Iterables.get(((CompoundTag) tag.get()).getAllKeys(), index));
        }
        return null;
    }

    public static class Factory extends ValueTypeListProxyNBTFactorySimple<ValueTypeString, ValueTypeString.ValueString, ValueTypeListProxyNbtKeys> {

        @Override
        public ResourceLocation getName() {
            return new ResourceLocation(Reference.MOD_ID, "nbt.keys");
        }

        @Override
        protected void serializeNbt(ValueTypeListProxyNbtKeys value, CompoundTag tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            value.tag.ifPresent(inbt -> tag.put("tag", inbt));
        }

        @Override
        protected ValueTypeListProxyNbtKeys deserializeNbt(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            return new ValueTypeListProxyNbtKeys(tag.contains("tag") ? Optional.of(tag.get("tag")) : Optional.empty());
        }
    }
}
