package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueParseRegistry;

/**
 * Collection of variable types.
 * @author rubensworks
 */
public class ValueParseMappings {

    public static final IValueParseRegistry REGISTRY = constructRegistry();

    private static IValueParseRegistry constructRegistry() {
        // This also allows this registry to be used outside of a minecraft environment.
        if(MinecraftHelpers.isModdedEnvironment()) {
            return IntegratedDynamics._instance.getRegistryManager().getRegistry(IValueParseRegistry.class);
        } else {
            return ValueParseRegistry.getInstance();
        }
    }

    public static void load() {
        REGISTRY.register(ValueTypes.STRING, ValueTypes.INTEGER, new ValueParseRegistry.IMapping<ValueTypeInteger, ValueTypeInteger.ValueInteger>() {
            @Override
            public ValueTypeInteger.ValueInteger parse(ValueTypeString.ValueString value) {
                try{
                    return ValueTypeInteger.ValueInteger.of(Integer.decode(value.getRawValue()));
                } catch (NumberFormatException e) {
                    return ValueTypeInteger.ValueInteger.of(0);
                }
            }
        });
        REGISTRY.register(ValueTypes.STRING, ValueTypes.DOUBLE, new ValueParseRegistry.IMapping<ValueTypeDouble, ValueTypeDouble.ValueDouble>() {
            @Override
            public ValueTypeDouble.ValueDouble parse(ValueTypeString.ValueString value) {
                try {
                    return ValueTypeDouble.ValueDouble.of(Double.parseDouble(value.getRawValue()));
                } catch (NumberFormatException e) {
                    try {
                        return ValueTypeDouble.ValueDouble.of((double) Long.decode(value.getRawValue()));
                    } catch (NumberFormatException e2) {
                        return ValueTypeDouble.ValueDouble.of(0.0);
                    }
                }
            }
        });
        REGISTRY.register(ValueTypes.STRING, ValueTypes.LONG, new ValueParseRegistry.IMapping<ValueTypeLong, ValueTypeLong.ValueLong>() {
            @Override
            public ValueTypeLong.ValueLong parse(ValueTypeString.ValueString value) {
                try {
                    return ValueTypeLong.ValueLong.of(Long.decode(value.getRawValue()));
                } catch (NumberFormatException e) {
                    return ValueTypeLong.ValueLong.of(0L);
                }
            }
        });
        REGISTRY.register(ValueTypes.STRING, ValueTypes.BOOLEAN, new ValueParseRegistry.IMapping<ValueTypeBoolean, ValueTypeBoolean.ValueBoolean>() {
            @Override
            public ValueTypeBoolean.ValueBoolean parse(ValueTypeString.ValueString value) {
                try {
                    return ValueTypeBoolean.ValueBoolean.of(Boolean.valueOf(value.getRawValue()));
                } catch (NumberFormatException e) {
                    return ValueTypeBoolean.ValueBoolean.of(false);
                }
            }
        });
        REGISTRY.register(ValueTypes.STRING, ValueTypes.NBT, new ValueParseRegistry.IMapping<ValueTypeNbt, ValueTypeNbt.ValueNbt>() {
            @Override
            public ValueTypeNbt.ValueNbt parse(ValueTypeString.ValueString value) {
                try {
                    return new ValueTypeNbt().deserialize(value.getRawValue());
                } catch (NumberFormatException e) {
                    return ValueTypeNbt.ValueNbt.of(null);
                }
            }
        });
    }

}
