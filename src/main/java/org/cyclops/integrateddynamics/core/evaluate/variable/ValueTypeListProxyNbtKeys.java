package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Iterables;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;

import java.util.Optional;

/**
 * A list of NBT keys.
 */
public class ValueTypeListProxyNbtKeys extends ValueTypeListProxyBase<ValueTypeString, ValueTypeString.ValueString> {

    private final Optional<INBT> tag;

    public ValueTypeListProxyNbtKeys(Optional<INBT> tag) {
        super(ValueTypeListProxyFactories.NBT_KEYS.getName(), ValueTypes.STRING);
        this.tag = tag;
    }

    @Override
    public int getLength() throws EvaluationException {
        return tag
                .map(t -> t instanceof CompoundNBT ? ((CompoundNBT) t).keySet().size() : 0)
                .orElse(0);
    }

    @Override
    public ValueTypeString.ValueString get(int index) throws EvaluationException {
        if (index < getLength()) {
            return ValueTypeString.ValueString.of(Iterables.get(((CompoundNBT) tag.get()).keySet(), index));
        }
        return null;
    }

    public static class Factory extends ValueTypeListProxyNBTFactorySimple<ValueTypeString, ValueTypeString.ValueString, ValueTypeListProxyNbtKeys> {

        @Override
        public ResourceLocation getName() {
            return new ResourceLocation(Reference.MOD_ID, "nbt.keys");
        }

        @Override
        protected void serializeNbt(ValueTypeListProxyNbtKeys value, CompoundNBT tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            value.tag.ifPresent(inbt -> tag.put("tag", inbt));
        }

        @Override
        protected ValueTypeListProxyNbtKeys deserializeNbt(CompoundNBT tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            return new ValueTypeListProxyNbtKeys(tag.contains("tag") ? Optional.of(tag.get("tag")) : Optional.empty());
        }
    }
}
