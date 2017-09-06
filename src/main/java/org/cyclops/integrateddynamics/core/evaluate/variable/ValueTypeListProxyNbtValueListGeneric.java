package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;

import java.util.Optional;

/**
 * An abstraction for a list of NBT values of a certain type.
 */
public abstract class ValueTypeListProxyNbtValueListGeneric<N extends NBTBase, T extends IValueType<V>, V extends IValue> extends ValueTypeListProxyBase<T, V> {

    private final String key;
    private final NBTTagCompound tag;

    public ValueTypeListProxyNbtValueListGeneric(String name, T valueType, String key, NBTTagCompound tag) {
        super(name, valueType);
        this.key = key;
        this.tag = tag;
    }

    public String getKey() {
        return key;
    }

    public NBTTagCompound getTag() {
        return tag;
    }

    @Override
    public int getLength() throws EvaluationException {
        return getLength(Optional.ofNullable((N) tag.getTag(key)).orElse(getDefault()));
    }

    @Override
    public V get(int index) throws EvaluationException {
        if (index < getLength()) {
            return get(Optional.ofNullable((N) tag.getTag(key)).orElse(getDefault()), index);
        }
        return null;
    }

    protected abstract int getLength(N tag);
    protected abstract V get(N tag, int index);
    protected abstract N getDefault();

    public static abstract class Factory<L extends ValueTypeListProxyNbtValueListGeneric<N, T, V>, N extends NBTBase, T extends IValueType<V>, V extends IValue> extends ValueTypeListProxyNBTFactorySimple<T, V, L> {

        @Override
        public String getName() {
            return "nbt.listValue";
        }

        @Override
        protected void serializeNbt(L value, NBTTagCompound tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            tag.setString("key", value.getKey());
            tag.setTag("tag", value.getTag());
        }

        @Override
        protected L deserializeNbt(NBTTagCompound tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            return create(tag.getString("key"), tag.getCompoundTag("tag"));
        }

        protected abstract L create(String key, NBTTagCompound tag);
    }
}
