package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IStringConversionRegistry;

/**
 * Collection of variable types.
 * @author rubensworks / LostOfThought
 */
public class StringConversionMappings {

    public static final IStringConversionRegistry REGISTRY = constructRegistry();

    private static IStringConversionRegistry constructRegistry() {
        // This also allows this registry to be used outside of a minecraft environment.
        if(MinecraftHelpers.isModdedEnvironment()) {
            return IntegratedDynamics._instance.getRegistryManager().getRegistry(IStringConversionRegistry.class);
        } else {
            return StringConversionRegistry.getInstance();
        }
    }

    public static void load() {
        REGISTRY.register(ValueTypes.INTEGER, ValueTypes.STRING, new IStringConversionRegistry.IMapping<ValueTypeInteger, ValueTypeString, ValueTypeInteger.ValueInteger, ValueTypeString.ValueString>() {
            @Override
            public ValueTypeString.ValueString convert(ValueTypeInteger.ValueInteger value) {
                return ValueTypeString.ValueString.of(String.valueOf(value.getRawValue()));
            }
        });
        REGISTRY.register(ValueTypes.DOUBLE, ValueTypes.STRING, new IStringConversionRegistry.IMapping<ValueTypeDouble, ValueTypeString, ValueTypeDouble.ValueDouble, ValueTypeString.ValueString>() {
            @Override
            public ValueTypeString.ValueString convert(ValueTypeDouble.ValueDouble value) {
                return ValueTypeString.ValueString.of(String.valueOf(value.getRawValue()));
            }
        });
        REGISTRY.register(ValueTypes.LONG, ValueTypes.STRING, new IStringConversionRegistry.IMapping<ValueTypeLong, ValueTypeString, ValueTypeLong.ValueLong, ValueTypeString.ValueString>() {
            @Override
            public ValueTypeString.ValueString convert(ValueTypeLong.ValueLong value) {
                return ValueTypeString.ValueString.of(String.valueOf(value.getRawValue()));
            }
        });
        REGISTRY.register(ValueTypes.NBT, ValueTypes.STRING, new IStringConversionRegistry.IMapping<ValueTypeNbt, ValueTypeString, ValueTypeNbt.ValueNbt, ValueTypeString.ValueString>() {
            @Override
            public ValueTypeString.ValueString convert(ValueTypeNbt.ValueNbt value) {
                return ValueTypeString.ValueString.of(new ValueTypeNbt().serialize(value));
            }
        });
        REGISTRY.register(ValueTypes.STRING, ValueTypes.INTEGER, new IStringConversionRegistry.IMapping<ValueTypeString, ValueTypeInteger, ValueTypeString.ValueString, ValueTypeInteger.ValueInteger>() {
            @Override
            public ValueTypeInteger.ValueInteger convert(ValueTypeString.ValueString value) {
                try{
                    return ValueTypeInteger.ValueInteger.of(Integer.decode(value.getRawValue()));
                } catch (NumberFormatException e) {
                    return ValueTypeInteger.ValueInteger.of(0);
                }
            }
        });
        REGISTRY.register(ValueTypes.STRING, ValueTypes.DOUBLE, new IStringConversionRegistry.IMapping<ValueTypeString, ValueTypeDouble, ValueTypeString.ValueString, ValueTypeDouble.ValueDouble>() {
            @Override
            public ValueTypeDouble.ValueDouble convert(ValueTypeString.ValueString value) {
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
        REGISTRY.register(ValueTypes.STRING, ValueTypes.LONG, new IStringConversionRegistry.IMapping<ValueTypeString, ValueTypeLong, ValueTypeString.ValueString, ValueTypeLong.ValueLong>() {
            @Override
            public ValueTypeLong.ValueLong convert(ValueTypeString.ValueString value) {
                try {
                    return ValueTypeLong.ValueLong.of(Long.decode(value.getRawValue()));
                } catch (NumberFormatException e) {
                    return ValueTypeLong.ValueLong.of(0L);
                }
            }
        });
        REGISTRY.register(ValueTypes.STRING, ValueTypes.NBT, new IStringConversionRegistry.IMapping<ValueTypeString, ValueTypeNbt, ValueTypeString.ValueString, ValueTypeNbt.ValueNbt>() {
            @Override
            public ValueTypeNbt.ValueNbt convert(ValueTypeString.ValueString value) {
                try {
                    return ValueTypeNbt.ValueNbt.of(new ValueTypeNbt().deserialize(value.getRawValue()).getRawValue());
                } catch (NumberFormatException e) {
                    return ValueTypeNbt.ValueNbt.of(null);
                }
            }
        });
        
    }

}