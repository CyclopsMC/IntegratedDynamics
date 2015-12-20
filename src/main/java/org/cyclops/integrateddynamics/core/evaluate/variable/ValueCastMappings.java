package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueCastRegistry;

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
                return ValueTypeDouble.ValueDouble.of((double) value.getRawValue());
            }
        });
        REGISTRY.register(ValueTypes.INTEGER, ValueTypes.LONG, new IValueCastRegistry.IMapping<ValueTypeInteger, ValueTypeLong, ValueTypeInteger.ValueInteger, ValueTypeLong.ValueLong>() {
            @Override
            public ValueTypeLong.ValueLong cast(ValueTypeInteger.ValueInteger value) {
                return ValueTypeLong.ValueLong.of((long) value.getRawValue());
            }
        });
        REGISTRY.register(ValueTypes.DOUBLE, ValueTypes.INTEGER, new IValueCastRegistry.IMapping<ValueTypeDouble, ValueTypeInteger, ValueTypeDouble.ValueDouble, ValueTypeInteger.ValueInteger>() {
            @Override
            public ValueTypeInteger.ValueInteger cast(ValueTypeDouble.ValueDouble value) {
                return ValueTypeInteger.ValueInteger.of((int) value.getRawValue());
            }
        });
        REGISTRY.register(ValueTypes.DOUBLE, ValueTypes.LONG, new IValueCastRegistry.IMapping<ValueTypeDouble, ValueTypeLong, ValueTypeDouble.ValueDouble, ValueTypeLong.ValueLong>() {
            @Override
            public ValueTypeLong.ValueLong cast(ValueTypeDouble.ValueDouble value) {
                return ValueTypeLong.ValueLong.of((long) value.getRawValue());
            }
        });
        REGISTRY.register(ValueTypes.LONG, ValueTypes.INTEGER, new IValueCastRegistry.IMapping<ValueTypeLong, ValueTypeInteger, ValueTypeLong.ValueLong, ValueTypeInteger.ValueInteger>() {
            @Override
            public ValueTypeInteger.ValueInteger cast(ValueTypeLong.ValueLong value) {
                return ValueTypeInteger.ValueInteger.of((int) value.getRawValue());
            }
        });
        REGISTRY.register(ValueTypes.LONG, ValueTypes.DOUBLE, new IValueCastRegistry.IMapping<ValueTypeLong, ValueTypeDouble, ValueTypeLong.ValueLong, ValueTypeDouble.ValueDouble>() {
            @Override
            public ValueTypeDouble.ValueDouble cast(ValueTypeLong.ValueLong value) {
                return ValueTypeDouble.ValueDouble.of((double) value.getRawValue());
            }
        });
    }

}
