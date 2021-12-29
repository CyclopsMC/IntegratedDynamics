package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;

/**
 * A sliced list.
 * @param <T> The value type type.
 * @param <V> The value type.
 */
public class ValueTypeListProxySlice<T extends IValueType<V>, V extends IValue> extends ValueTypeListProxyBase<T, V> {

    private final IValueTypeListProxy<T, V> list;
    private final int from;
    private final int to;

    public ValueTypeListProxySlice(IValueTypeListProxy<T, V> list, int from, int to) {
        super(ValueTypeListProxyFactories.SLICE.getName(), list.getValueType());
        this.list = list;
        this.from = from;
        this.to = to;
    }

    @Override
    public int getLength() throws EvaluationException {
        return Math.max(0, Math.min(list.getLength(), this.to) - this.from);
    }

    @Override
    public V get(int index) throws EvaluationException {
        if (index < list.getLength()) {
            return list.get(this.from + index);
        }
        return null;
    }

    public static class Factory extends ValueTypeListProxyNBTFactorySimple<IValueType<IValue>, IValue, ValueTypeListProxySlice<IValueType<IValue>, IValue>> {

        @Override
        public ResourceLocation getName() {
            return new ResourceLocation(Reference.MOD_ID, "slice");
        }

        @Override
        protected void serializeNbt(ValueTypeListProxySlice<IValueType<IValue>, IValue> value, CompoundTag tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            tag.put("sublist", ValueTypeListProxyFactories.REGISTRY.serialize(value.list));
            tag.putInt("from", value.from);
            tag.putInt("to", value.to);
        }

        @Override
        protected ValueTypeListProxySlice<IValueType<IValue>, IValue> deserializeNbt(CompoundTag tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            IValueTypeListProxy<IValueType<IValue>, IValue> list = ValueTypeListProxyFactories.REGISTRY.deserialize(tag.get("sublist"));
            return new ValueTypeListProxySlice<>(list, tag.getInt("from"), tag.getInt("to"));
        }
    }
}
