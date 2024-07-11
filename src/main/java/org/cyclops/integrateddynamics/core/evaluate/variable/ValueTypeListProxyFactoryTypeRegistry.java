package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxy;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;

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
    public <T extends IValueType<V>, V extends IValue, P extends IValueTypeListProxy<T, V>> IProxyFactory<T, V, P> getFactory(ResourceLocation name) {
        return factories.get(name.toString());
    }

    @Override
    public <T extends IValueType<V>, V extends IValue, P extends IValueTypeListProxy<T, V>> Tag serialize(ValueDeseralizationContext valueDeseralizationContext, P proxy) throws SerializationException {
        IProxyFactory<T, V, P> factory = getFactory(proxy.getName());
        if(factory == null) {
            throw new SerializationException(String.format("No serialization factory exists for the list proxy type name '%s'.", proxy.getName()));
        }
        Tag serialized = factory.serialize(valueDeseralizationContext, proxy);
        CompoundTag tag = new CompoundTag();
        tag.putString("proxyName", proxy.getName().toString());
        tag.put("serialized", serialized);
        return tag;
    }

    @Override
    public <T extends IValueType<V>, V extends IValue, P extends IValueTypeListProxy<T, V>> P deserialize(ValueDeseralizationContext valueDeseralizationContext, Tag value) throws SerializationException {
        if (!(value instanceof CompoundTag)) {
            throw new SerializationException(String.format("Could not deserialize the serialized list proxy value '%s' as it is not a CompoundTag.", value));
        }
        CompoundTag tag = (CompoundTag) value;
        if (!tag.contains("proxyName", Tag.TAG_STRING)) {
            throw new SerializationException(String.format("Could not deserialize the serialized list proxy value '%s' as it is missing a proxyName.", value));
        }
        if (!tag.contains("serialized")) {
            throw new SerializationException(String.format("Could not deserialize the serialized list proxy value '%s' as it is missing a serialized value.", value));
        }
        String name = tag.getString("proxyName");
        Tag actualValue = tag.get("serialized");
        IProxyFactory<T, V, P> factory = getFactory(ResourceLocation.parse(name));
        if(factory == null) {
            throw new SerializationException(String.format("No deserialization factory exists for the list proxy type name '%s'.", name));
        }
        return factory.deserialize(valueDeseralizationContext, actualValue);
    }
}
