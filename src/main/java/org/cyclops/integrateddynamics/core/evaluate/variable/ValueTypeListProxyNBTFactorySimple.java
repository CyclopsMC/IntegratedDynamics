package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    public void serialize(ValueOutput valueOutput, P value) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        serializeNbt(valueOutput, value);
    }

    @Override
    public P deserialize(ValueInput valueInput) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException {
        try {
            return deserializeNbt(valueInput);
        } catch (ClassCastException | EvaluationException e) {
            e.printStackTrace();
            throw new IValueTypeListProxyFactoryTypeRegistry.SerializationException(e.getMessage());
        }
    }

    protected abstract void serializeNbt(ValueOutput valueOutput, P value) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException;
    protected abstract P deserializeNbt(ValueInput valueInput) throws IValueTypeListProxyFactoryTypeRegistry.SerializationException, EvaluationException;
}
