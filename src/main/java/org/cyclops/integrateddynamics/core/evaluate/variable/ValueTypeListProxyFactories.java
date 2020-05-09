package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.util.ResourceLocation;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
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
    public static ValueTypeListProxyNBTFactory<ValueObjectTypeItemStack, ValueObjectTypeItemStack.ValueItemStack, ValueTypeListProxyEntityItems> ENTITY_CAPABILITY_ITEMS;
    public static ValueTypeListProxyNBTFactory<ValueObjectTypeFluidStack, ValueObjectTypeFluidStack.ValueFluidStack, ValueTypeListProxyEntityFluids> ENTITY_CAPABILITY_FLUIDS;
    public static ValueTypeListProxyNBTFactory<ValueObjectTypeRecipe, ValueObjectTypeRecipe.ValueRecipe, ValueTypeListProxyPositionedRecipes> POSITIONED_RECIPES;
    public static ValueTypeListProxyAppend.Factory APPEND;
    public static ValueTypeListProxyConcat.Factory CONCAT;
    public static ValueTypeListProxyLazyBuilt.Factory LAZY_BUILT;
    public static ValueTypeListProxyTail.Factory TAIL;
    public static ValueTypeListProxySlice.Factory SLICE;
    public static ValueTypeListProxyNbtKeys.Factory NBT_KEYS;
    public static ValueTypeListProxyNbtValueListTag.Factory NBT_VALUE_LIST_TAG;
    public static ValueTypeListProxyNbtValueListByte.Factory NBT_VALUE_LIST_BYTE;
    public static ValueTypeListProxyNbtValueListInt.Factory NBT_VALUE_LIST_INT;
    public static ValueTypeListProxyNbtValueListLong.Factory NBT_VALUE_LIST_LONG;
    public static ValueTypeListProxyNbtAsListTag.Factory NBT_AS_LIST_TAG;
    public static ValueTypeListProxyNbtAsListByte.Factory NBT_AS_LIST_BYTE;
    public static ValueTypeListProxyNbtAsListInt.Factory NBT_AS_LIST_INT;
    public static ValueTypeListProxyNbtAsListLong.Factory NBT_AS_LIST_LONG;

    public static void load() {
        if(MATERIALIZED == null) {
            MATERIALIZED = REGISTRY.register(new ValueTypeListProxyMaterializedFactory());
            POSITIONED_INVENTORY = REGISTRY.register(new ValueTypeListProxyNBTFactory<>(new ResourceLocation(Reference.MOD_ID, "positioned_inventory"), ValueTypeListProxyPositionedInventory.class));
            ENTITY_ARMORINVENTORY = REGISTRY.register(new ValueTypeListProxyNBTFactory<>(new ResourceLocation(Reference.MOD_ID, "entity_armor_inventory"), ValueTypeListProxyEntityArmorInventory.class));
            ENTITY_INVENTORY = REGISTRY.register(new ValueTypeListProxyNBTFactory<>(new ResourceLocation(Reference.MOD_ID, "entity_inventory"), ValueTypeListProxyEntityInventory.class));
            POSITIONED_TANK_FLUIDSTACKS = REGISTRY.register(new ValueTypeListProxyNBTFactory<>(new ResourceLocation(Reference.MOD_ID, "positioned_tank_fluidstacks"), ValueTypeListProxyPositionedTankFluidStacks.class));
            POSITIONED_TANK_CAPACITIES = REGISTRY.register(new ValueTypeListProxyNBTFactory<>(new ResourceLocation(Reference.MOD_ID, "positioned_tank_capacities"), ValueTypeListProxyPositionedTankCapacities.class));
            ENTITY_CAPABILITY_ITEMS = REGISTRY.register(new ValueTypeListProxyNBTFactory<>(new ResourceLocation(Reference.MOD_ID, "entity_capability_items"), ValueTypeListProxyEntityItems.class));
            ENTITY_CAPABILITY_FLUIDS = REGISTRY.register(new ValueTypeListProxyNBTFactory<>(new ResourceLocation(Reference.MOD_ID, "entity_capability_fluids"), ValueTypeListProxyEntityFluids.class));
            POSITIONED_RECIPES = REGISTRY.register(new ValueTypeListProxyNBTFactory<>(new ResourceLocation(Reference.MOD_ID, "positioned_recipes"), ValueTypeListProxyPositionedRecipes.class));
            APPEND = REGISTRY.register(new ValueTypeListProxyAppend.Factory());
            CONCAT = REGISTRY.register(new ValueTypeListProxyConcat.Factory());
            LAZY_BUILT = REGISTRY.register(new ValueTypeListProxyLazyBuilt.Factory());
            TAIL = REGISTRY.register(new ValueTypeListProxyTail.Factory());
            SLICE = REGISTRY.register(new ValueTypeListProxySlice.Factory());
            NBT_KEYS = REGISTRY.register(new ValueTypeListProxyNbtKeys.Factory());
            NBT_VALUE_LIST_TAG = REGISTRY.register(new ValueTypeListProxyNbtValueListTag.Factory());
            NBT_VALUE_LIST_BYTE = REGISTRY.register(new ValueTypeListProxyNbtValueListByte.Factory());
            NBT_VALUE_LIST_INT = REGISTRY.register(new ValueTypeListProxyNbtValueListInt.Factory());
            NBT_VALUE_LIST_LONG = REGISTRY.register(new ValueTypeListProxyNbtValueListLong.Factory());
            NBT_AS_LIST_TAG = REGISTRY.register(new ValueTypeListProxyNbtAsListTag.Factory());
            NBT_AS_LIST_BYTE = REGISTRY.register(new ValueTypeListProxyNbtAsListByte.Factory());
            NBT_AS_LIST_INT = REGISTRY.register(new ValueTypeListProxyNbtAsListInt.Factory());
            NBT_AS_LIST_LONG = REGISTRY.register(new ValueTypeListProxyNbtAsListLong.Factory());
        }
    }

}
