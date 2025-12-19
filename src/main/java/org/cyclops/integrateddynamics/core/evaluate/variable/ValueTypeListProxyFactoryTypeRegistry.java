package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Maps;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;

import java.util.Map;

/**
 * Registry for list value type proxies.
 * @author rubensworks
 */
public class ValueTypeListProxyFactoryTypeRegistry implements IValueTypeListProxyFactoryTypeRegistry {

    private static ValueTypeListProxyFactoryTypeRegistry INSTANCE = new ValueTypeListProxyFactoryTypeRegistry();

    private final Map<String, IProxyFactory> factories = Maps.newHashMap();

    private ValueTypeListProxyFactoryTypeRegistry() {

    }

    /**
     * @return The unique instance.
     */
    public static ValueTypeListProxyFactoryTypeRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public <T extends IValueType<V>, V extends IValue, P extends IValueTypeListProxy<T, V>, F extends IProxyFactory<T, V, P>> F register(F proxyFactory) {
        if(factories.containsKey(proxyFactory.getName())) {
            throw new RuntimeException(String.format("A list proxy factory by name '%s' already exists.", proxyFactory.getName()));
        }
        factories.put(proxyFactory.getName().toString(), proxyFactory);
        return proxyFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IValueType<V>, V extends IValue, P extends IValueTypeListProxy<T, V>> IProxyFactory<T, V, P> getFactory(Identifier name) {
        return factories.get(name.toString());
    }

    @Override
    public <T extends IValueType<V>, V extends IValue, P extends IValueTypeListProxy<T, V>> void serialize(ValueOutput valueOutput, P proxy) throws SerializationException {
        IProxyFactory<T, V, P> factory = getFactory(proxy.getName());
        if (factory == null) {
            throw new SerializationException(String.format("No serialization factory exists for the list proxy type name '%s'.", proxy.getName()));
        }
        valueOutput.putString("proxyName", proxy.getName().toString());
        factory.serialize(valueOutput.child("serialized"), proxy);
    }

    @Override
    public <T extends IValueType<V>, V extends IValue, P extends IValueTypeListProxy<T, V>> P deserialize(ValueInput valueInput) throws SerializationException {
        String name = valueInput.getString("proxyName").orElseThrow();
        IProxyFactory<T, V, P> factory = getFactory(Identifier.parse(name));
        if (factory == null) {
            throw new SerializationException(String.format("No deserialization factory exists for the list proxy type name '%s'.", name));
        }
        return factory.deserialize(valueInput.child("serialized").orElseThrow());
    }
}
