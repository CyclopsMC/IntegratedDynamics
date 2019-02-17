package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueParseRegistry;
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
        REGISTRY.register(ValueTypes.DOUBLE, value -> {
            try {
                return ValueTypeDouble.ValueDouble.of(Double.parseDouble(value.getRawValue()));
            } catch (NumberFormatException e) {
                try {
                    return ValueTypeDouble.ValueDouble.of((double) Long.decode(value.getRawValue()));
                } catch (NumberFormatException e2) {
                    return ValueTypeDouble.ValueDouble.of(0.0);
                }
            }
        });
        REGISTRY.register(ValueTypes.LONG, value -> {
            try {
                return ValueTypeLong.ValueLong.of(Long.decode(value.getRawValue()));
            } catch (NumberFormatException e) {
                return ValueTypeLong.ValueLong.of(0L);
            }
        });
        REGISTRY.register(ValueTypes.BOOLEAN, value -> {
            // Should be more robust, ([Tt](rue)?|[Ff](alse)?)
            try {
                return ValueTypeBoolean.ValueBoolean.of(Boolean.valueOf(value.getRawValue()));
            } catch (NumberFormatException e) {
                try {
                    return ValueTypeBoolean.ValueBoolean.of(Long.decode(value.getRawValue()) != 0);
                } catch (NumberFormatException e2) {
                    return ValueTypeBoolean.ValueBoolean.of(false);
                }
            }
        });
        REGISTRY.register(ValueTypes.NBT, value -> {
            try {
                return new ValueTypeNbt().deserialize(value.getRawValue());
            } catch (IllegalArgumentException e) {
                return ValueTypeNbt.ValueNbt.of(null);
            }
        });
    }

}
