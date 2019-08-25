package org.cyclops.integrateddynamics.api.evaluate.variable;

import org.cyclops.cyclopscore.init.IRegistry;
import org.cyclops.integrateddynamics.api.evaluate.InvalidValueTypeException;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;

/**
 * Registry for parseing {@link IValue}.
 * @author rubensworks/LostOfThought
 */
public interface IValueParseRegistry extends IRegistry {

    /**
     * Register a mapping between to value types.
     * @param to The target type.
     * @param mapping The mapping logic.
     * @param <T2> The target type type.
     * @param <V2> The target type.
     */
    public <T2 extends IValueType<V2>, V2 extends IValue> void register(
            T2 to, IMapping<T2, V2> mapping);


    /**
     * Parse the given value to the given type.
     * @param target The target type.
     * @param value The value to parse.
     * @param <T2> The target type type.
     * @param <V2> The target type.
     * @return The parse value
     * @throws ValueParseException If parsing failed because the type mapping did not exist.
     */
    public <T2 extends IValueType<V2>, V2 extends IValue> V2 parse(T2 target, ValueTypeString.ValueString value) throws ValueParseException;

    /**
     * Check if the the given value can be parse to the given type.
     * @param target The target type.
     * @param <T2> The target type type.
     * @param <V2> The target type.
     * @return If the value can be parse to the given type.
     */
    public <T2 extends IValueType<V2>, V2 extends IValue> boolean canParse(T2 target);

    public static interface IMapping<T2 extends IValueType<V2>, V2 extends IValue> {

        public V2 parse(ValueTypeString.ValueString value);

    }

    public static class ValueParseException extends InvalidValueTypeException {

        public ValueParseException(IValueType to) {
            super(String.format("No parser exists for %s", to));
        }
    }

}
