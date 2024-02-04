package org.cyclops.integrateddynamics.api.evaluate.variable;

import net.minecraft.network.chat.Component;
import org.cyclops.cyclopscore.init.IRegistry;
import org.cyclops.integrateddynamics.api.evaluate.InvalidValueTypeException;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

/**
 * Registry for casting {@link IValue}.
 * @author rubensworks
 */
public interface IValueCastRegistry extends IRegistry {

    public int size();

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
     * Cast the given value to the given type.
     * @param target The target type.
     * @param value The value to cast.
     * @param <T1> The source type type.
     * @param <T2> The target type type.
     * @param <V1> The source type.
     * @param <V2> The target type.
     * @return The cast value
     * @throws ValueCastException If casting failed because the type mapping did not exist.
     */
    public <T1 extends IValueType<V1>, T2 extends IValueType<V2>, V1 extends IValue, V2 extends IValue> V2 cast(T2 target, V1 value) throws ValueCastException;

    /**
     * Check if the the given value can be cast to the given type.
     * @param target The target type.
     * @param value The value to cast.
     * @param <T1> The source type type.
     * @param <T2> The target type type.
     * @param <V1> The source type.
     * @param <V2> The target type.
     * @return If the value can be cast to the given type.
     */
    public <T1 extends IValueType<V1>, T2 extends IValueType<V2>, V1 extends IValue, V2 extends IValue> boolean canCast(T2 target, V1 value);

    public static interface IMapping<T1 extends IValueType<V1>, T2 extends IValueType<V2>, V1 extends IValue, V2 extends IValue> {

        public V2 cast(V1 value);

    }

    public static class ValueCastException extends InvalidValueTypeException {

        public ValueCastException(IValueType from, IValueType to) {
            super(Component.translatable(L10NValues.OPERATOR_ERROR_CAST_NOMAPPING, from, to));
        }
    }

}
