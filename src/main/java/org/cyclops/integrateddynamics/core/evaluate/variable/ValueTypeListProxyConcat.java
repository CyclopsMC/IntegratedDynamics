package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;

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
        public String getName() {
            return "concat";
        }

        @Override
        protected void serializeNbt(ValueTypeListProxyConcat<IValueType<IValue>, IValue> value, NBTTagCompound tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            NBTTagList list = new NBTTagList();
            for (IValueTypeListProxy<IValueType<IValue>, IValue> listProxy : value.lists) {
                list.appendTag(new NBTTagString(ValueTypeListProxyFactories.REGISTRY.serialize(listProxy)));
            }
            tag.setTag("sublists", list);
        }

        @Override
        protected ValueTypeListProxyConcat<IValueType<IValue>, IValue> deserializeNbt(NBTTagCompound tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            NBTTagList list = tag.getTagList("sublists", MinecraftHelpers.NBTTag_Types.NBTTagString.ordinal());
            IValueTypeListProxy<IValueType<IValue>, IValue>[] listProxies = new IValueTypeListProxy[list.tagCount()];
            for (int i = 0; i < list.tagCount(); i++) {
                listProxies[i] = ValueTypeListProxyFactories.REGISTRY.deserialize(list.getStringTagAt(i));
            }
            return new ValueTypeListProxyConcat<>(listProxies);
        }
    }
}
