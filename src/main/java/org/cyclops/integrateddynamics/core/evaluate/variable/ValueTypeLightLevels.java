package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeLightLevelRegistry;

/**
 * Collection of light level calculators for value types..
 * @author rubensworks
 */
public class ValueTypeLightLevels {

    public static final IValueTypeLightLevelRegistry REGISTRY = constructRegistry();

    private static IValueTypeLightLevelRegistry constructRegistry() {
        // This also allows this registry to be used outside of a minecraft environment.
        if(MinecraftHelpers.isModdedEnvironment()) {
            return IntegratedDynamics._instance.getRegistryManager().getRegistry(IValueTypeLightLevelRegistry.class);
        } else {
            return ValueTypeLightLevelRegistry.getInstance();
        }
    }

    public static void load() {
        REGISTRY.register(ValueTypes.INTEGER, new IValueTypeLightLevelRegistry.ILightLevelCalculator<ValueTypeInteger.ValueInteger>() {
            @Override
            public int getLightLevel(ValueTypeInteger.ValueInteger value) {
                return Math.max(0, Math.min(value.getRawValue(), 15));
            }
        });
        REGISTRY.register(ValueTypes.BOOLEAN, new IValueTypeLightLevelRegistry.ILightLevelCalculator<ValueTypeBoolean.ValueBoolean>() {
            @Override
            public int getLightLevel(ValueTypeBoolean.ValueBoolean value) {
                return value.getRawValue() ? 15 : 0;
            }
        });
    }

}
