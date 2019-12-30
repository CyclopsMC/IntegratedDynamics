package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;

/**
 * A base class for list proxy factories that use NBT to store data.
 * @author rubensworks
 */
public abstract class ValueTypeListProxyNBTFactorySimple<T extends IValueType<V>, V extends IValue, P extends IValueTypeListProxy<T, V>> implements IValueTypeListProxyFactoryTypeRegistry.IProxyFactory<T, V, P> {

    @Override
    public INBT serialize(P value) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        CompoundNBT tag = new CompoundNBT();
        serializeNbt(value, tag);
        return tag;
    }

    @Override
    public P deserialize(INBT value) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        try {
            return deserializeNbt((CompoundNBT) value);
        } catch (ClassCastException | EvaluationException e) {
            e.printStackTrace();
            throw new IValueTypeListProxyFactoryTypeRegistry.SerializationException(e.getMessage());
        }
    }

    protected abstract void serializeNbt(P value, CompoundNBT tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException;
    protected abstract P deserializeNbt(CompoundNBT tag) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException, EvaluationException;
}
