package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author rubensworks
 */
public class ValueTypeLightLevelRegistry implements IValueTypeLightLevelRegistry {

    private static ValueTypeLightLevelRegistry INSTANCE = new ValueTypeLightLevelRegistry();

    private final Map<IValueType<?>, ILightLevelCalculator> lightLevelCalculatorMap = Maps.newHashMap();

    private ValueTypeLightLevelRegistry() {

    }

    /**
     * @return The unique instance.
     */
    public static ValueTypeLightLevelRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public <L extends ILightLevelCalculator<V>, V extends IValue> L register(IValueType<V> valueType, L lightLevelCalculator) {
        lightLevelCalculatorMap.put(valueType, lightLevelCalculator);
        return lightLevelCalculator;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends IValue> ILightLevelCalculator<V> getLightLevelCalculator(IValueType<V> valueType) {
        return lightLevelCalculatorMap.get(valueType);
    }
}
