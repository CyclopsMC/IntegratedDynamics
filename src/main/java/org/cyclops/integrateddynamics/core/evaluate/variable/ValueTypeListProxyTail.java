package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;

/**
 * An list without its first element.
 * @param <T> The value type type.
 * @param <V> The value type.
 */
public class ValueTypeListProxyTail<T extends IValueType<V>, V extends IValue> extends ValueTypeListProxyBase<T, V> {

    private final IValueTypeListProxy<T, V> list;

    public ValueTypeListProxyTail(IValueTypeListProxy<T, V> list) {
        super(ValueTypeListProxyFactories.TAIL.getName(), list.getValueType());
        this.list = list;
    }

    @Override
    public int getLength() throws EvaluationException {
        return Math.max(0, list.getLength() - 1);
    }

    @Override
    public V get(int index) throws EvaluationException {
        int listLength = list.getLength();
        if (index < listLength - 1) {
            return list.get(index + 1);
        }
        return null;
    }

    public static class Factory extends ValueTypeListProxyNBTFactorySimple<IValueType<IValue>, IValue, ValueTypeListProxyTail<IValueType<IValue>, IValue>> {

        @Override
        public ResourceLocation getName() {
            return new ResourceLocation(Reference.MOD_ID, "tail");
        }

        @Override
        protected void serializeNbt(ValueTypeListProxyTail<IValueType<IValue>, IValue> value, CompoundTag tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            tag.put("sublist", ValueTypeListProxyFactories.REGISTRY.serialize(value.list));
        }

        @Override
        protected ValueTypeListProxyTail<IValueType<IValue>, IValue> deserializeNbt(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException, EvaluationException {
            IValueTypeListProxy<IValueType<IValue>, IValue> list = ValueTypeListProxyFactories.REGISTRY.deserialize(valueDeseralizationContext, tag.get("sublist"));
            return new ValueTypeListProxyTail<>(list);
        }
    }
}
