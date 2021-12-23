package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Maps;
import net.minecraft.util.text.TranslationTextComponent;
import org.cyclops.integrateddynamics.api.evaluate.InvalidValueTypeException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeLightLevelRegistry;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import javax.annotation.Nullable;
import java.util.Map;

import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeLightLevelRegistry.ILightLevelCalculator;

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
    @Nullable
    @Override
    public <V extends IValue> ILightLevelCalculator<V> getLightLevelCalculator(IValueType<V> valueType) {
        return lightLevelCalculatorMap.get(valueType);
    }

    @Override
    public <V extends IValue> int getLightLevel(V value) throws InvalidValueTypeException {
        IValueType<V> valueType = value.getType();
        ILightLevelCalculator<V> lightLevelCalculator = getLightLevelCalculator(valueType);
        if(lightLevelCalculator != null) {
            return lightLevelCalculator.getLightLevel(value);
        }
        for (Map.Entry<IValueType<?>, ILightLevelCalculator> entry : lightLevelCalculatorMap.entrySet()) {
            if(value.canCast(entry.getKey())) {
                return entry.getValue().getLightLevel(value.cast(entry.getKey()));
            }
        }
        throw new InvalidValueTypeException(new TranslationTextComponent(L10NValues.VALUETYPE_ERROR_NOLIGHTCALCULATOR, value));
    }

}
