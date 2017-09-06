package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Iterables;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;

/**
 * A list of NBT keys.
 */
public class ValueTypeListProxyNbtKeys extends ValueTypeListProxyBase<ValueTypeString, ValueTypeString.ValueString> {

    private final NBTTagCompound tag;

    public ValueTypeListProxyNbtKeys(NBTTagCompound tag) {
        super(ValueTypeListProxyFactories.NBT_KEYS.getName(), ValueTypes.STRING);
        this.tag = tag;
    }

    @Override
    public int getLength() throws EvaluationException {
        return tag.getKeySet().size();
    }

    @Override
    public ValueTypeString.ValueString get(int index) throws EvaluationException {
        if (index < tag.getSize()) {
            return ValueTypeString.ValueString.of(Iterables.get(tag.getKeySet(), index));
        }
        return null;
    }

    public static class Factory extends ValueTypeListProxyNBTFactorySimple<ValueTypeString, ValueTypeString.ValueString, ValueTypeListProxyNbtKeys> {

        @Override
        public String getName() {
            return "nbt.keys";
        }

        @Override
        protected void serializeNbt(ValueTypeListProxyNbtKeys value, NBTTagCompound tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            tag.setTag("tag", value.tag);
        }

        @Override
        protected ValueTypeListProxyNbtKeys deserializeNbt(NBTTagCompound tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            return new ValueTypeListProxyNbtKeys(tag.getCompoundTag("tag"));
        }
    }
}
