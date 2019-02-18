package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueParseRegistry;

import java.util.regex.Pattern;

/**
 * Collection of variable types.
 * @author rubensworks/lostofthought
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
        REGISTRY.register(ValueTypes.INTEGER, value -> {
            try{
                return ValueTypeInteger.ValueInteger.of(Integer.decode(value.getRawValue()));
            } catch (NumberFormatException e) {
                return ValueTypeInteger.ValueInteger.of(0);
            }
        });
        REGISTRY.register(ValueTypes.LONG, value -> {
            try {
                return ValueTypeLong.ValueLong.of(Long.decode(value.getRawValue()));
            } catch (NumberFormatException e) {
                return ValueTypeLong.ValueLong.of(0L);
            }
        });
        REGISTRY.register(ValueTypes.DOUBLE, value -> {
            // TODO: /[+-]?Inf(inity)?/i
            // TODO: Floating point Hex/Octal
            try {
                return ValueTypeDouble.ValueDouble.of(Double.parseDouble(value.getRawValue()));
            } catch (NumberFormatException e) {
                try {
                    // Try as a long
                    return ValueTypeDouble.ValueDouble.of((double) Long.decode(value.getRawValue()));
                } catch (NumberFormatException e2) {
                    return ValueTypeDouble.ValueDouble.of(0.0);
                }
            }
        });
        REGISTRY.register(ValueTypes.BOOLEAN, value -> {
            try {
                Pattern p = Pattern.compile("\\AF(alse)?\\z", Pattern.CASE_INSENSITIVE);
                if( value.getRawValue().isEmpty()
                    || p.matcher(value.getRawValue()).matches()
                    || (Long.decode(value.getRawValue()) == 0))
                {
                    return ValueTypeBoolean.ValueBoolean.of(false);
                } else {
                    return ValueTypeBoolean.ValueBoolean.of(true);
                }
            } catch (NumberFormatException e) {
                return ValueTypeBoolean.ValueBoolean.of(true);
            }
        });
        REGISTRY.register(ValueTypes.STRING, value -> value);
        REGISTRY.register(ValueTypes.NBT, value -> {
            try {
                return new ValueTypeNbt().deserialize(value.getRawValue());
            } catch (IllegalArgumentException e) {
                return ValueTypeNbt.ValueNbt.of(null);
            }
        });
    }

}
