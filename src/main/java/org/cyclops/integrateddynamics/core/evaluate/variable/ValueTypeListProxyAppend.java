package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
            return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "append");
        }

        @Override
        protected void serializeNbt(ValueOutput valueOutput, ValueTypeListProxyAppend<IValueType<IValue>, IValue> value) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            valueOutput.putString("valueType", value.value.getType().getUniqueName().toString());
            ValueHelpers.serializeRaw(valueOutput.child("v"), value.value);
            ValueTypeListProxyFactories.REGISTRY.serialize(valueOutput.child("sublist"), value.list);
        }

        @Override
        protected ValueTypeListProxyAppend<IValueType<IValue>, IValue> deserializeNbt(ValueInput valueInput) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            IValueType valueType = ValueTypes.REGISTRY.getValueType(ResourceLocation.parse(valueInput.getString("valueType").orElseThrow()));
            IValue value = ValueHelpers.deserializeRaw(valueInput.child("v").orElseThrow(), valueType);
            IValueTypeListProxy<IValueType<IValue>, IValue> list = ValueTypeListProxyFactories.REGISTRY.deserialize(valueInput.child("sublist").orElseThrow());
            return new ValueTypeListProxyAppend<>(list, value);
        }
    }
}
