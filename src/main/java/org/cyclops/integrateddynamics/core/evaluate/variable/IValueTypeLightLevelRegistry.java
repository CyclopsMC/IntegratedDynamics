package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.cyclopscore.init.IRegistry;

/**
 * Registry for mapping value types to their light level calculator.
 * @author rubensworks
 */
public interface IValueTypeLightLevelRegistry extends IRegistry {

    /**
     * Register light level calculator for a value type.
     * @param valueType The value type
     * @param lightLevelCalculator The light level calculator.
     * @param <L> The light level calculator type.
     * @param <V> The value type.
     * @return The registered light level calculator.
     */
    public <L extends ILightLevelCalculator<V>, V extends IValue> L register(IValueType<V> valueType, L lightLevelCalculator);

    /**
     * Get the light level calculator for the given value type.
     * @param valueType The value type
     * @return The registered light level calculator.
     */
    public <V extends IValue> ILightLevelCalculator<V> getLightLevelCalculator(IValueType<V> valueType);

    public static interface ILightLevelCalculator<V extends IValue> {

        public int getLightLevel(V value);

    }

}
