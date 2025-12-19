package org.cyclops.integrateddynamics.api.evaluate.variable;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    public <T extends IValueType<V>, V extends IValue, P extends IValueTypeListProxy<T, V>> IProxyFactory<T, V, P> getFactory(Identifier name);

    /**
     * Serialize the given list proxy.
     *
     * @param <T>         The list element type value type.
     * @param <V>         The list element type.
     * @param <P>         The proxy type.
     * @param valueOutput The value to output to.
     * @param proxy       The proxy to serialize.
     * @throws SerializationException If something goes wrong while serializing.
     */
    public <T extends IValueType<V>, V extends IValue, P extends IValueTypeListProxy<T, V>> void serialize(ValueOutput valueOutput, P proxy) throws SerializationException;

    /**
     * Deserialize the given serialized list proxy to a list proxy instance.
     *
     * @param <T>        The list element type value type.
     * @param <V>        The list element type.
     * @param <P>        The proxy type.
     * @param valueInput The serialized list proxy.
     * @return The deserialized list proxy.
     * @throws SerializationException If something goes wrong while serializing.
     */
    public <T extends IValueType<V>, V extends IValue, P extends IValueTypeListProxy<T, V>> P deserialize(ValueInput valueInput) throws SerializationException;

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
        public Identifier getName();

        /**
         * Serialize the given value.
         *
         * @param valueOutput Where to serialize to.
         * @param value       The value to serialize.
         * @throws SerializationException If something goes wrong while serializing.
         */
        public void serialize(ValueOutput valueOutput, P value) throws SerializationException;

        /**
         * Deserialize the given value.
         *
         * @param valueInput The value to deserialize.
         * @return The deserialized value.
         * @throws SerializationException If something goes wrong while deserializing.
         */
        public P deserialize(ValueInput valueInput) throws SerializationException;

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
