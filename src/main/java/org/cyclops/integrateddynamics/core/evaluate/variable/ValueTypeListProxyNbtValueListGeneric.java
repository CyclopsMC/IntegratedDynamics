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
    private final Optional<CompoundNBT> tag;

    public ValueTypeListProxyNbtValueListGeneric(ResourceLocation name, T valueType, String key, Optional<INBT> tag) {
        super(name, valueType);
        this.key = key;
        this.tag = tag.filter(t -> t instanceof CompoundNBT).map(t -> (CompoundNBT) t);
    }

    public String getKey() {
        return key;
    }

    public Optional<CompoundNBT> getTag() {
        return tag;
    }

    @Override
    public int getLength() throws EvaluationException {
        try {
            return getTag()
                    .map(t -> Optional.ofNullable((N) t.get(key)))
                    .orElse(Optional.empty())
                    .map(this::getLength)
                    .orElse(0);
        } catch (ClassCastException e) {
            return 0;
        }
    }

    @Override
    public V get(int index) throws EvaluationException {
        try {
            if (index < getLength()) {
                return getTag()
                        .map(t -> Optional.ofNullable((N) t.get(key)))
                        .orElse(Optional.empty())
                        .map(t -> get(t, index))
                        .orElse(null);
            }
        } catch (ClassCastException e) {}
        return null;
    }

    protected abstract int getLength(N tag);
    protected abstract V get(N tag, int index);

    public static abstract class Factory<L extends ValueTypeListProxyNbtValueListGeneric<N, T, V>, N extends INBT, T extends IValueType<V>, V extends IValue> extends ValueTypeListProxyNBTFactorySimple<T, V, L> {

        @Override
        public ResourceLocation getName() {
            return new ResourceLocation(Reference.MOD_ID, "nbt.list_value");
        }

        @Override
        protected void serializeNbt(L value, CompoundNBT tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            tag.putString("key", value.getKey());
            if (value.getTag().isPresent()) {
                tag.put("tag", value.getTag().get());
            }
        }

        @Override
        protected L deserializeNbt(CompoundNBT tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            return create(tag.getString("key"), Optional.ofNullable(tag.get("tag")));
        }

        protected abstract L create(String key, Optional<INBT> tag);
    }
}
