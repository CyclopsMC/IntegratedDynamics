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
    public static ValueTypeListProxyNBTFactory<ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack, ValueTypeListProxyEntityArmorInventory> ENTITY_ARMORINVENTORY;
    public static ValueTypeListProxyNBTFactory<ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack, ValueTypeListProxyEntityInventory> ENTITY_INVENTORY;
    public static ValueTypeListProxyNBTFactory<ValueObjectTypeFluidStack, ValueObjectTypeFluidStack.ValueFluidStack, ValueTypeListProxyPositionedTankFluidStacks> POSITIONED_TANK_FLUIDSTACKS;
    public static ValueTypeListProxyNBTFactory<ValueTypeInteger, ValueTypeInteger.ValueInteger, ValueTypeListProxyPositionedTankCapacities> POSITIONED_TANK_CAPACITIES;
    public static ValueTypeListProxyAppend.Factory APPEND;
    public static ValueTypeListProxyConcat.Factory CONCAT;
    public static ValueTypeListProxyLazyBuilt.Factory LAZY_BUILT;
    public static ValueTypeListProxyTail.Factory TAIL;
    public static ValueTypeListProxySlice.Factory SLICE;

    public static void load() {
        if(MATERIALIZED == null) {
            MATERIALIZED = REGISTRY.register(new ValueTypeListProxyMaterializedFactory());
            POSITIONED_INVENTORY = REGISTRY.register(new ValueTypeListProxyNBTFactory<>("positionedInventory", ValueTypeListProxyPositionedInventory.class));
            ENTITY_ARMORINVENTORY = REGISTRY.register(new ValueTypeListProxyNBTFactory<>("entityArmorInventory", ValueTypeListProxyEntityArmorInventory.class));
            ENTITY_INVENTORY = REGISTRY.register(new ValueTypeListProxyNBTFactory<>("entityInventory", ValueTypeListProxyEntityInventory.class));
            POSITIONED_TANK_FLUIDSTACKS = REGISTRY.register(new ValueTypeListProxyNBTFactory<>("positionedTankFluidstacks", ValueTypeListProxyPositionedTankFluidStacks.class));
            POSITIONED_TANK_CAPACITIES = REGISTRY.register(new ValueTypeListProxyNBTFactory<>("positionedTankCapacities", ValueTypeListProxyPositionedTankCapacities.class));
            APPEND = REGISTRY.register(new ValueTypeListProxyAppend.Factory());
            CONCAT = REGISTRY.register(new ValueTypeListProxyConcat.Factory());
            LAZY_BUILT = REGISTRY.register(new ValueTypeListProxyLazyBuilt.Factory());
            TAIL = REGISTRY.register(new ValueTypeListProxyTail.Factory());
            SLICE = REGISTRY.register(new ValueTypeListProxySlice.Factory());
        }
    }

}
