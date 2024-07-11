package org.cyclops.integrateddynamics.api.evaluate.variable;

import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.cyclopscore.init.IRegistry;

/**
 * Registry for list value type proxies.
 * @author rubensworks
 */
public interface IValueTypeListProxyFactoryTypeRegistry extends IRegistry {

    /**
     * Register a proxy factory by name.
     * @param proxyFactory The proxy factory.
     * @param <T> The list element type value type.
     * @param <V> The list element type.
     * @param <P> The proxy type.
     * @param <F> The factory type.
     * @return The registered instance.
     */
    public <T extends IValueType<V>, V extends IValue, P extends IValueTypeListProxy<T, V>, F extends IProxyFactory<T, V, P>> F register(F proxyFactory);

    /**
     * Get a proxy factory by name
     * @param <T> The list element type value type.
     * @param <V> The list element type.
     * @param <P> The proxy type.
     * @param name The name.
     * @return The corresponding instance.
     */
    public <T extends IValueType<V>, V extends IValue, P extends IValueTypeListProxy<T, V>> IProxyFactory<T, V, P> getFactory(ResourceLocation name);

    /**
     * Serialize the given list proxy.
     *
     * @param <T>                        The list element type value type.
     * @param <V>                        The list element type.
     * @param <P>                        The proxy type.
     * @param valueDeseralizationContext
     * @param proxy                      The proxy to serialize.
     * @return The serialized string.
     * @throws SerializationException If something goes wrong while serializing.
     */
    public <T extends IValueType<V>, V extends IValue, P extends IValueTypeListProxy<T, V>> Tag serialize(ValueDeseralizationContext valueDeseralizationContext, P proxy) throws SerializationException;

    /**
     * Deserialize the given serialized list proxy to a list proxy instance.
     * @param <T> The list element type value type.
     * @param <V> The list element type.
     * @param <P> The proxy type.
     * @param valueDeseralizationContext
     * @param value The serialized list proxy.
     * @return The deserialized list proxy.
     * @throws SerializationException If something goes wrong while serializing.
     */
    public <T extends IValueType<V>, V extends IValue, P extends IValueTypeListProxy<T, V>> P deserialize(ValueDeseralizationContext valueDeseralizationContext, Tag value) throws SerializationException;

    /**
     * Factory for a list proxy.
     * @param <T> The list element type value type.
     * @param <V> The list element type.
     * @param <P> The proxy type.
     */
    public static interface IProxyFactory<T extends IValueType<V>, V extends IValue, P extends IValueTypeListProxy<T, V>> {

        /**
         * @return The unique indentifier of this proxy.
         */
        public ResourceLocation getName();

        /**
         * Serialize the given value.
         *
         * @param valueDeseralizationContext
         * @param value                      The value to serialize.
         * @return The serialized value.
         * @throws SerializationException If something goes wrong while serializing.
         */
        public Tag serialize(ValueDeseralizationContext valueDeseralizationContext, P value) throws SerializationException;

        /**
         * Deserialize the given value.
         *
         * @param valueDeseralizationContext
         * @param value The value to deserialize.
         * @return The deserialized value.
         * @throws SerializationException If something goes wrong while deserializing.
         */
        public P deserialize(ValueDeseralizationContext valueDeseralizationContext, Tag value) throws SerializationException;

    }

    /**
     * If something goes wrong while (de)serializing.
     */
    public static class SerializationException extends Exception {

        public SerializationException(String message) {
            super(message);
        }

    }

}
