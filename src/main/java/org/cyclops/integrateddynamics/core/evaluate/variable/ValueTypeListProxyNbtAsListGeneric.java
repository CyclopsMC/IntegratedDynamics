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
 * An abstraction casting an NBT value to a list of a certain type.
 */
public abstract class ValueTypeListProxyNbtAsListGeneric<N extends INBT, T extends IValueType<V>, V extends IValue> extends ValueTypeListProxyBase<T, V> {

    private final Optional<INBT> tag;

    public ValueTypeListProxyNbtAsListGeneric(ResourceLocation name, T valueType, Optional<INBT> tag) {
        super(name, valueType);
        this.tag = tag;
    }

    public Optional<INBT> getTag() {
        return tag;
    }

    @Override
    public int getLength() throws EvaluationException {
        try {
            return getTag()
                    .map(t -> getLength((N) t))
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
                        .map(t -> get((N) t, index))
                        .orElse(null);
            }
        } catch (ClassCastException e) {}
        return null;
    }

    protected abstract int getLength(N tag);
    protected abstract V get(N tag, int index);

    public static abstract class Factory<L extends ValueTypeListProxyNbtAsListGeneric<N, T, V>, N extends INBT, T extends IValueType<V>, V extends IValue> extends ValueTypeListProxyNBTFactorySimple<T, V, L> {

        @Override
        public ResourceLocation getName() {
            return new ResourceLocation(Reference.MOD_ID, "nbt.list_as_value");
        }

        @Override
        protected void serializeNbt(L value, CompoundNBT tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            if (value.getTag().isPresent()) {
                tag.put("tag", value.getTag().get());
            }
        }

        @Override
        protected L deserializeNbt(CompoundNBT tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            return create(Optional.ofNullable(tag.get("tag")));
        }

        protected abstract L create(Optional<INBT> tag);
    }
}
