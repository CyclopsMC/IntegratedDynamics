package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.apache.commons.compress.utils.Lists;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;

import java.util.List;

/**
 * A concatenated list.
 * @param <T> The value type type.
 * @param <V> The value type.
 */
public class ValueTypeListProxyConcat<T extends IValueType<V>, V extends IValue> extends ValueTypeListProxyBase<T, V> {

    private final IValueTypeListProxy<T, V>[] lists;

    public ValueTypeListProxyConcat(IValueTypeListProxy<T, V>... lists) {
        super(ValueTypeListProxyFactories.CONCAT.getName(), lists[0].getValueType());
        this.lists = lists;
    }

    @Override
    public int getLength() throws EvaluationException {
        int length = 0;
        for (IValueTypeListProxy<T, V> list : lists) {
            length += list.getLength();
        }
        return length;
    }

    @Override
    public V get(int index) throws EvaluationException {
        for (IValueTypeListProxy<T, V> list : lists) {
            int currentLength = list.getLength();
            if (index < currentLength) {
                return list.get(index);
            }
            index -= currentLength;
        }
        return null;
    }

    public static class Factory extends ValueTypeListProxyNBTFactorySimple<IValueType<IValue>, IValue, ValueTypeListProxyConcat<IValueType<IValue>, IValue>> {

        @Override
        public ResourceLocation getName() {
            return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "concat");
        }

        @Override
        protected void serializeNbt(ValueOutput valueOutput, ValueTypeListProxyConcat<IValueType<IValue>, IValue> value) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            ValueOutput.ValueOutputList list = valueOutput.childrenList("sublists");
            for (IValueTypeListProxy<IValueType<IValue>, IValue> listProxy : value.lists) {
                ValueTypeListProxyFactories.REGISTRY.serialize(list.addChild(), listProxy);
            }
        }

        @Override
        protected ValueTypeListProxyConcat<IValueType<IValue>, IValue> deserializeNbt(ValueInput valueInput) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            List<IValueTypeListProxy<IValueType<IValue>, IValue>> listProxies = Lists.newArrayList();
            for (ValueInput child : valueInput.childrenList("sublists").orElseThrow()) {
                listProxies.add(ValueTypeListProxyFactories.REGISTRY.deserialize(child));
            }
            return new ValueTypeListProxyConcat<>(listProxies.toArray(IValueTypeListProxy[]::new));
        }
    }
}
