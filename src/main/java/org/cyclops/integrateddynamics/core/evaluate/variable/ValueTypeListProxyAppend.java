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

    public static class Factory implements IValueTypeListProxyFactoryTypeRegistry.IProxyFactory<IValueType<IValue>, IValue, ValueTypeListProxyAppend<IValueType<IValue>, IValue>> {

        @Override
        public String getName() {
            return "append";
        }

        @Override
        public String serialize(ValueTypeListProxyAppend<IValueType<IValue>, IValue> values) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("valueType", values.value.getType().getUnlocalizedName());
            tag.setString("value", values.value.getType().serialize(values.value));
            tag.setString("sublist", ValueTypeListProxyFactories.REGISTRY.serialize(values.list));
            return tag.toString();
        }

        @Override
        public ValueTypeListProxyAppend<IValueType<IValue>, IValue> deserialize(String data) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            try {
                NBTTagCompound tag = JsonToNBT.getTagFromJson(data);
                IValueType valueType = ValueTypes.REGISTRY.getValueType(tag.getString("valueType"));
                IValue value = valueType.deserialize(tag.getString("value"));
                IValueTypeListProxy list = ValueTypeListProxyFactories.REGISTRY.deserialize(tag.getString("sublist"));
                return new ValueTypeListProxyAppend<>(list, value);
            } catch (NBTException e) {
                e.printStackTrace();
                throw new IValueTypeListProxyFactoryTypeRegistry.SerializationException(e.getMessage());
            }
        }
    }
}
