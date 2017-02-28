package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
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

    public static class Factory implements IValueTypeListProxyFactoryTypeRegistry.IProxyFactory<IValueType<IValue>, IValue, ValueTypeListProxyTail<IValueType<IValue>, IValue>> {

        @Override
        public String getName() {
            return "tail";
        }

        @Override
        public String serialize(ValueTypeListProxyTail<IValueType<IValue>, IValue> values) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("sublist", ValueTypeListProxyFactories.REGISTRY.serialize(values.list));
            return tag.toString();
        }

        @Override
        public ValueTypeListProxyTail<IValueType<IValue>, IValue> deserialize(String data) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            try {
                NBTTagCompound tag = JsonToNBT.getTagFromJson(data);
                IValueTypeListProxy<IValueType<IValue>, IValue> list = ValueTypeListProxyFactories.REGISTRY.deserialize(tag.getString("sublist"));
                return new ValueTypeListProxyTail<>(list);
            } catch (NBTException e) {
                e.printStackTrace();
                throw new IValueTypeListProxyFactoryTypeRegistry.SerializationException(e.getMessage());
            }
        }
    }
}
