package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Maps;
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

    private static final String TYPE_DELIMITER = ";";
    private static final String TYPE_DELIMITER_SPLITREGEX = "(?<!\\\\);";
    private static final String TYPE_DELIMITER_ESCAPED = "\\\\;";

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
    public <T extends IValueType<V>, V extends IValue, P extends IValueTypeListProxy<T, V>> IProxyFactory<T, V, P> register(IProxyFactory<T, V, P> proxyFactory) {
        if(factories.containsKey(proxyFactory.getName())) {
            throw new RuntimeException(String.format("A list proxy factory by name '%s' already exists.", proxyFactory.getName()));
        }
        factories.put(proxyFactory.getName(), proxyFactory);
        return proxyFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IValueType<V>, V extends IValue, P extends IValueTypeListProxy<T, V>> IProxyFactory<T, V, P> getFactory(String name) {
        return factories.get(name);
    }

    @Override
    public <T extends IValueType<V>, V extends IValue, P extends IValueTypeListProxy<T, V>> String serialize(P proxy) throws SerializationException {
        IProxyFactory<T, V, P> factory = getFactory(proxy.getName());
        if(factory == null) {
            throw new SerializationException(String.format("No serialization factory exists for the list proxy type name '%s'.", proxy.getName()));
        }
        String serialized = factory.serialize(proxy);
        return proxy.getName() + TYPE_DELIMITER + serialized.replaceAll(TYPE_DELIMITER, TYPE_DELIMITER_ESCAPED);
    }

    @Override
    public <T extends IValueType<V>, V extends IValue, P extends IValueTypeListProxy<T, V>> P deserialize(String value) throws SerializationException {
        String[] split = value.split(TYPE_DELIMITER_SPLITREGEX);
        if(split.length != 2) {
            throw new SerializationException(String.format("Could not deserialize the serialized list proxy value '%s'.", value));
        }
        String name = split[0];
        String actualValue = split[1].replaceAll(TYPE_DELIMITER_ESCAPED, TYPE_DELIMITER);
        IProxyFactory<T, V, P> factory = getFactory(name);
        if(factory == null) {
            throw new SerializationException(String.format("No deserialization factory exists for the list proxy type name '%s'.", name));
        }
        return factory.deserialize(actualValue);
    }
}
