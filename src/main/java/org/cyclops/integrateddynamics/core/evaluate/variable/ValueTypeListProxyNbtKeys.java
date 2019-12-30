package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Iterables;
import net.minecraft.nbt.CompoundNBT;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;

/**
 * A list of NBT keys.
 */
public class ValueTypeListProxyNbtKeys extends ValueTypeListProxyBase<ValueTypeString, ValueTypeString.ValueString> {

    private final CompoundNBT tag;

    public ValueTypeListProxyNbtKeys(CompoundNBT tag) {
        super(ValueTypeListProxyFactories.NBT_KEYS.getName(), ValueTypes.STRING);
        this.tag = tag;
    }

    @Override
    public int getLength() throws EvaluationException {
        return tag.keySet().size();
    }

    @Override
    public ValueTypeString.ValueString get(int index) throws EvaluationException {
        if (index < tag.size()) {
            return ValueTypeString.ValueString.of(Iterables.get(tag.keySet(), index));
        }
        return null;
    }

    public static class Factory extends ValueTypeListProxyNBTFactorySimple<ValueTypeString, ValueTypeString.ValueString, ValueTypeListProxyNbtKeys> {

        @Override
        public String getName() {
            return "nbt.keys";
        }

        @Override
        protected void serializeNbt(ValueTypeListProxyNbtKeys value, CompoundNBT tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            tag.put("tag", value.tag);
        }

        @Override
        protected ValueTypeListProxyNbtKeys deserializeNbt(CompoundNBT tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            return new ValueTypeListProxyNbtKeys(tag.getCompound("tag"));
        }
    }
}
