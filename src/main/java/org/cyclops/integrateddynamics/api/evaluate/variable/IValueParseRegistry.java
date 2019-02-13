package org.cyclops.integrateddynamics.api.evaluate.variable;

import org.cyclops.cyclopscore.init.IRegistry;
import org.cyclops.integrateddynamics.api.evaluate.InvalidValueTypeException;

/**
 * Registry for parsing {@link IValue}.
 * @author rubensworks / LostOfThought
 */
public interface IValueParseRegistry extends IRegistry {

    /**
     * Register a mapping between to value types.
     * @param from The source type.
     * @param to The target type.
     * @param mapping The mapping logic.
     * @param <T1> The source type type.
     * @param <T2> The target type type.
     * @param <V1> The source type.
     * @param <V2> The target type.
     */
    public <T1 extends IValueType<V1>, T2 extends IValueType<V2>, V1 extends IValue, V2 extends IValue> void register(
            T1 from, T2 to, IMapping<T1, T2, V1, V2> mapping);


    /**
     * Parse the given value to the given type.
     * @param target The target type.
     * @param value The value to parse.
     * @param <T1> The source type type.
     * @param <T2> The target type type.
     * @param <V1> The source type.
     * @param <V2> The target type.
     * @return The parse value
     * @throws ValueParseException If parsing failed because the type mapping did not exist.
     */
    public <T1 extends IValueType<V1>, T2 extends IValueType<V2>, V1 extends IValue, V2 extends IValue> V2 parse(T2 target, V1 value) throws ValueParseException;

    /**
     * Check if the the given value can be parsed to the given type.
     * @param target The target type.
     * @param value The value to parse.
     * @param <T1> The source type type.
     * @param <T2> The target type type.
     * @param <V1> The source type.
     * @param <V2> The target type.
     * @return If the value can be parsed to the given type.
     */
    public <T1 extends IValueType<V1>, T2 extends IValueType<V2>, V1 extends IValue, V2 extends IValue> boolean canParse(T2 target, V1 value);

    public static interface IMapping<T1 extends IValueType<V1>, T2 extends IValueType<V2>, V1 extends IValue, V2 extends IValue> {

        public V2 parse(V1 value);

    }

    public static class ValueParseException extends InvalidValueTypeException {

        public ValueParseException(IValueType from, IValueType to) {
            super(String.format("No string parse mapping exists from %s to %s", from, to));
        }
    }

}
