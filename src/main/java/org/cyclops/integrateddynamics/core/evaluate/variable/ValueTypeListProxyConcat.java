package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;

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
        protected void serializeNbt(ValueDeseralizationContext valueDeseralizationContext, ValueTypeListProxyConcat<IValueType<IValue>, IValue> value, CompoundTag tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            ListTag list = new ListTag();
            for (IValueTypeListProxy<IValueType<IValue>, IValue> listProxy : value.lists) {
                list.add(ValueTypeListProxyFactories.REGISTRY.serialize(valueDeseralizationContext, listProxy));
            }
            tag.put("sublists", list);
        }

        @Override
        protected ValueTypeListProxyConcat<IValueType<IValue>, IValue> deserializeNbt(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            ListTag list = (ListTag) tag.get("sublists");
            IValueTypeListProxy<IValueType<IValue>, IValue>[] listProxies = new IValueTypeListProxy[list.size()];
            for (int i = 0; i < list.size(); i++) {
                listProxies[i] = ValueTypeListProxyFactories.REGISTRY.deserialize(valueDeseralizationContext, list.get(i));
            }
            return new ValueTypeListProxyConcat<>(listProxies);
        }
    }
}
