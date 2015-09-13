package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Collection of variable types.
 * @author rubensworks
 */
public class ValueCastMappings {

    public static final IValueCastRegistry REGISTRY = constructRegistry();

    private static IValueCastRegistry constructRegistry() {
        // This also allows this registry to be used outside of a minecraft environment.
        if(MinecraftHelpers.isModdedEnvironment()) {
            return IntegratedDynamics._instance.getRegistryManager().getRegistry(IValueCastRegistry.class);
        } else {
            return ValueCastRegistry.getInstance();
        }
    }

    public static void load() {
        REGISTRY.register(ValueTypes.INTEGER, ValueTypes.DOUBLE, new IValueCastRegistry.IMapping<ValueTypeInteger, ValueTypeDouble, ValueTypeInteger.ValueInteger, ValueTypeDouble.ValueDouble>() {
            @Override
            public ValueTypeDouble.ValueDouble cast(ValueTypeInteger.ValueInteger value) {
                return ValueTypeDouble.ValueDouble.of(value.getRawValue());
            }
        });
        REGISTRY.register(ValueTypes.DOUBLE, ValueTypes.INTEGER, new IValueCastRegistry.IMapping<ValueTypeDouble, ValueTypeInteger, ValueTypeDouble.ValueDouble, ValueTypeInteger.ValueInteger>() {
            @Override
            public ValueTypeInteger.ValueInteger cast(ValueTypeDouble.ValueDouble value) {
                return ValueTypeInteger.ValueInteger.of((int) value.getRawValue());
            }
        });
    }

}
