package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;

import java.util.Optional;

/**
 * An abstraction casting an NBT value to a list of a certain type.
 */
public abstract class ValueTypeListProxyNbtAsListGeneric<N extends Tag, T extends IValueType<V>, V extends IValue> extends ValueTypeListProxyBase<T, V> {

    private final Optional<Tag> tag;

    public ValueTypeListProxyNbtAsListGeneric(ResourceLocation name, T valueType, Optional<Tag> tag) {
        super(name, valueType);
        this.tag = tag;
    }

    public Optional<Tag> getTag() {
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

    public static abstract class Factory<L extends ValueTypeListProxyNbtAsListGeneric<N, T, V>, N extends Tag, T extends IValueType<V>, V extends IValue> extends ValueTypeListProxyNBTFactorySimple<T, V, L> {

        @Override
        public ResourceLocation getName() {
            return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "nbt.list_as_value");
        }

        @Override
        protected void serializeNbt(ValueOutput valueOutput, L value) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            value.getTag().ifPresent(tag -> valueOutput.store("tag", ExtraCodecs.NBT, tag));
        }

        @Override
        protected L deserializeNbt(ValueInput valueInput) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
            return create(valueInput.read("tag", ExtraCodecs.NBT));
        }

        protected abstract L create(Optional<Tag> tag);
    }
}
