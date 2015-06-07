package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Collection of variable types.
 * @author rubensworks
 */
public class ValueTypes {

    public static final IValueTypeRegistry REGISTRY = IntegratedDynamics._instance.getRegistryManager().getRegistry(IValueTypeRegistry.class);

    public static ValueTypeBoolean BOOLEAN = REGISTRY.register(new ValueTypeBoolean());

}
