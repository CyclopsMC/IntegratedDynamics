package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;

/**
 * An appended list.
 * @param <T> The value type type.
 * @param <V> The value type.
 */
public class ValueTypeListProxyAppend<T extends IValueType<V>, V extends IValue> extends ValueTypeListProxyBase<T, V> {

    private final IValueTypeListProxy<T, V> list;
    private final V value;

    public ValueTypeListProxyAppend(IValueTypeListProxy<T, V> list, V value) {
        super(ValueTypeListProxyFactories.APPEND.getName(), list.getValueType());
        this.list = list;
        this.value = value;
    }

    @Override
    public int getLength() throws EvaluationException {
        return list.getLength() + 1;
    }

    @Override
    public V get(int index) throws EvaluationException {
        int listLength = list.getLength();
        if (index < listLength) {
            return list.get(index);
        } else if (index == listLength) {
            return value;
        }
        return null;
    }

    public static class Factory extends ValueTypeListProxyNBTFactorySimple<IValueType<IValue>, IValue, ValueTypeListProxyAppend<IValueType<IValue>, IValue>> {

        @Override
        public ResourceLocation getName() {
            return new ResourceLocation(Reference.MOD_ID, "append");
        }

        @Override
        protected void serializeNbt(ValueTypeListProxyAppend<IValueType<IValue>, IValue> value, CompoundNBT tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            tag.putString("valueType", value.value.getType().getUniqueName().toString());
            tag.put("value", ValueHelpers.serializeRaw(value.value));
            tag.put("sublist", ValueTypeListProxyFactories.REGISTRY.serialize(value.list));
        }

        @Override
        protected ValueTypeListProxyAppend<IValueType<IValue>, IValue> deserializeNbt(CompoundNBT tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            IValueType valueType = ValueTypes.REGISTRY.getValueType(new ResourceLocation(tag.getString("valueType")));
            IValue value = ValueHelpers.deserializeRaw(valueType, tag.get("value"));
            IValueTypeListProxy<IValueType<IValue>, IValue> list = ValueTypeListProxyFactories.REGISTRY.deserialize(tag.get("sublist"));
            return new ValueTypeListProxyAppend<>(list, value);
        }
    }
}
