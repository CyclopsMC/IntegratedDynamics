package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.item.IVariableFacade;

/**
 * Collection of variable types.
 * @author rubensworks
 */
public class ValueTypes {

    public static final IValueTypeRegistry REGISTRY = constructRegistry();

    public static ValueTypeAny     ANY     = REGISTRY.register(new ValueTypeAny());
    public static ValueTypeBoolean BOOLEAN = REGISTRY.register(new ValueTypeBoolean());
    public static ValueTypeInteger INTEGER = REGISTRY.register(new ValueTypeInteger());

    private static IValueTypeRegistry constructRegistry() {
        // This also allows this registry to be used outside of a minecraft environment.
        if(MinecraftHelpers.isModdedEnvironment()) {
            return IntegratedDynamics._instance.getRegistryManager().getRegistry(IValueTypeRegistry.class);
        } else {
            return ValueTypeRegistry.getInstance();
        }
    }

    public static void load() {}

    public static IValueType[] from(IVariable[] variables) {
        IValueType[] valueTypes = new IValueType[variables.length];
        for(int i = 0; i < valueTypes.length; i++) {
            IVariable variable = variables[i];
            valueTypes[i] = variable == null ? null : variable.getType();
        }
        return valueTypes;
    }

    public static IValueType[] from(IVariableFacade[] variableFacades) {
        IValueType[] valueTypes = new IValueType[variableFacades.length];
        for(int i = 0; i < valueTypes.length; i++) {
            IVariableFacade variableFacade = variableFacades[i];
            valueTypes[i] = variableFacade == null ? null : variableFacade.getOutputType();
        }
        return valueTypes;
    }

}
