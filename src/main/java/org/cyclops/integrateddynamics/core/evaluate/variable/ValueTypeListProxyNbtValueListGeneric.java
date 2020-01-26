package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;

import java.util.Optional;

/**
 * An abstraction for a list of NBT values of a certain type.
 */
public abstract class ValueTypeListProxyNbtValueListGeneric<N extends INBT, T extends IValueType<V>, V extends IValue> extends ValueTypeListProxyBase<T, V> {

    private final String key;
    private final CompoundNBT tag;

    public ValueTypeListProxyNbtValueListGeneric(ResourceLocation name, T valueType, String key, CompoundNBT tag) {
        super(name, valueType);
        this.key = key;
        this.tag = tag;
    }

    public String getKey() {
        return key;
    }

    public CompoundNBT getTag() {
        return tag;
    }

    @Override
    public int getLength() throws EvaluationException {
        try {
            return getLength(Optional.ofNullable((N) tag.get(key)).orElse(getDefault()));
        } catch (ClassCastException e) {
            return 0;
        }
    }

    @Override
    public V get(int index) throws EvaluationException {
        try {
            if (index < getLength()) {
                return get(Optional.ofNullable((N) tag.get(key)).orElse(getDefault()), index);
            }
        } catch (ClassCastException e) {}
        return null;
    }

    protected abstract int getLength(N tag);
    protected abstract V get(N tag, int index);
    protected abstract N getDefault();

    public static abstract class Factory<L extends ValueTypeListProxyNbtValueListGeneric<N, T, V>, N extends INBT, T extends IValueType<V>, V extends IValue> extends ValueTypeListProxyNBTFactorySimple<T, V, L> {

        @Override
        public ResourceLocation getName() {
            return new ResourceLocation(Reference.MOD_ID, "nbt.list_value");
        }

        @Override
        protected void serializeNbt(L value, CompoundNBT tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            tag.putString("key", value.getKey());
            tag.put("tag", value.getTag());
        }

        @Override
        protected L deserializeNbt(CompoundNBT tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            return create(tag.getString("key"), tag.getCompound("tag"));
        }

        protected abstract L create(String key, CompoundNBT tag);
    }
}
