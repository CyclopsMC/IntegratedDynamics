package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;

/**
 * A base class for list proxy factories that use NBT to store data.
 * @author rubensworks
 */
public abstract class ValueTypeListProxyNBTFactorySimple<T extends IValueType<V>, V extends IValue, P extends IValueTypeListProxy<T, V>> implements IValueTypeListProxyFactoryTypeRegistry.IProxyFactory<T, V, P> {

    @Override
    public Tag serialize(P value) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        CompoundTag tag = new CompoundTag();
        serializeNbt(value, tag);
        return tag;
    }

    @Override
    public P deserialize(ValueDeseralizationContext valueDeseralizationContext, Tag value) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        try {
            return deserializeNbt(valueDeseralizationContext, (CompoundTag) value);
        } catch (ClassCastException | EvaluationException e) {
            e.printStackTrace();
            throw new IValueTypeListProxyFactoryTypeRegistry.SerializationException(e.getMessage());
        }
    }

    protected abstract void serializeNbt(P value, CompoundTag tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException;
    protected abstract P deserializeNbt(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException, EvaluationException;
}
