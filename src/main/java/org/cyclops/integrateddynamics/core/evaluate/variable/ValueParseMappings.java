package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueParseRegistry;

/**
 * Collection of variable types.
 * @author rubensworks / LostOfThought
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
        REGISTRY.register(ValueTypes.INTEGER, ValueTypes.STRING, new IValueParseRegistry.IMapping<ValueTypeInteger, ValueTypeString, ValueTypeInteger.ValueInteger, ValueTypeString.ValueString>() {
            @Override
            public ValueTypeString.ValueString parse(ValueTypeInteger.ValueInteger value) {
                return ValueTypeString.ValueString.of(String.valueOf(value.getRawValue()));
            }
        });
        REGISTRY.register(ValueTypes.DOUBLE, ValueTypes.STRING, new IValueParseRegistry.IMapping<ValueTypeDouble, ValueTypeString, ValueTypeDouble.ValueDouble, ValueTypeString.ValueString>() {
            @Override
            public ValueTypeString.ValueString parse(ValueTypeDouble.ValueDouble value) {
                return ValueTypeString.ValueString.of(String.valueOf(value.getRawValue()));
            }
        });
        REGISTRY.register(ValueTypes.LONG, ValueTypes.STRING, new IValueParseRegistry.IMapping<ValueTypeLong, ValueTypeString, ValueTypeLong.ValueLong, ValueTypeString.ValueString>() {
            @Override
            public ValueTypeString.ValueString parse(ValueTypeLong.ValueLong value) {
                return ValueTypeString.ValueString.of(String.valueOf(value.getRawValue()));
            }
        });
        REGISTRY.register(ValueTypes.NBT, ValueTypes.STRING, new IValueParseRegistry.IMapping<ValueTypeNbt, ValueTypeString, ValueTypeNbt.ValueNbt, ValueTypeString.ValueString>() {
            @Override
            public ValueTypeString.ValueString parse(ValueTypeNbt.ValueNbt value) {
                return ValueTypeString.ValueString.of(new ValueTypeNbt().serialize(value));
            }
        });
        REGISTRY.register(ValueTypes.STRING, ValueTypes.INTEGER, new IValueParseRegistry.IMapping<ValueTypeString, ValueTypeInteger, ValueTypeString.ValueString, ValueTypeInteger.ValueInteger>() {
            @Override
            public ValueTypeInteger.ValueInteger parse(ValueTypeString.ValueString value) {
                try{
                    return ValueTypeInteger.ValueInteger.of(Integer.decode(value.getRawValue()));
                } catch (NumberFormatException e) {
                    return ValueTypeInteger.ValueInteger.of(0);
                }
            }
        });
        REGISTRY.register(ValueTypes.STRING, ValueTypes.DOUBLE, new IValueParseRegistry.IMapping<ValueTypeString, ValueTypeDouble, ValueTypeString.ValueString, ValueTypeDouble.ValueDouble>() {
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
        REGISTRY.register(ValueTypes.STRING, ValueTypes.LONG, new IValueParseRegistry.IMapping<ValueTypeString, ValueTypeLong, ValueTypeString.ValueString, ValueTypeLong.ValueLong>() {
            @Override
            public ValueTypeLong.ValueLong parse(ValueTypeString.ValueString value) {
                try {
                    return ValueTypeLong.ValueLong.of(Long.decode(value.getRawValue()));
                } catch (NumberFormatException e) {
                    return ValueTypeLong.ValueLong.of(0L);
                }
            }
        });
        REGISTRY.register(ValueTypes.STRING, ValueTypes.NBT, new IValueParseRegistry.IMapping<ValueTypeString, ValueTypeNbt, ValueTypeString.ValueString, ValueTypeNbt.ValueNbt>() {
            @Override
            public ValueTypeNbt.ValueNbt parse(ValueTypeString.ValueString value) {
                try {
                    return ValueTypeNbt.ValueNbt.of(new ValueTypeNbt().deserialize(value.getRawValue()).getRawValue());
                } catch (NumberFormatException e) {
                    return ValueTypeNbt.ValueNbt.of(null);
                }
            }
        });
        
    }

}