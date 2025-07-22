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
            return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "tail");
        }

        @Override
        protected void serializeNbt(ValueOutput valueOutput, ValueTypeListProxyTail<IValueType<IValue>, IValue> value) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            ValueTypeListProxyFactories.REGISTRY.serialize(valueOutput.child("sublist"), value.list);
        }

        @Override
        protected ValueTypeListProxyTail<IValueType<IValue>, IValue> deserializeNbt(ValueInput valueInput) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException, EvaluationException {
            IValueTypeListProxy<IValueType<IValue>, IValue> list = ValueTypeListProxyFactories.REGISTRY.deserialize(valueInput.child("sublist").orElseThrow());
            return new ValueTypeListProxyTail<>(list);
        }
    }
}
