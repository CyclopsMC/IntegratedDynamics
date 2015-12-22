package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;

/**
 * Collection of light level calculators for value types..
 * @author rubensworks
 */
public class ValueTypeListProxyFactories {

    public static final IValueTypeListProxyFactoryTypeRegistry REGISTRY = constructRegistry();

    private static IValueTypeListProxyFactoryTypeRegistry constructRegistry() {
        // This also allows this registry to be used outside of a minecraft environment.
        if(MinecraftHelpers.isModdedEnvironment()) {
            return IntegratedDynamics._instance.getRegistryManager().getRegistry(IValueTypeListProxyFactoryTypeRegistry.class);
        } else {
            return ValueTypeListProxyFactoryTypeRegistry.getInstance();
        }
    }

    public static ValueTypeListProxyMaterializedFactory MATERIALIZED;
    public static ValueTypeListProxyNBTFactory<ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack, ValueTypeListProxyPositionedInventory> POSITIONED_INVENTORY;

    public static void load() {
        MATERIALIZED = REGISTRY.register(new ValueTypeListProxyMaterializedFactory());
        POSITIONED_INVENTORY = REGISTRY.register(new ValueTypeListProxyNBTFactory<>("positionedInventory", ValueTypeListProxyPositionedInventory.class));
    }

}
