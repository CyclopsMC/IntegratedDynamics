package org.cyclops.integrateddynamics.core.evaluate.variable;

import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeRegistry;

/**
 * Collection of variable types.
 * @author rubensworks
 */
public class ValueTypes {

    public static final IValueTypeRegistry REGISTRY = constructRegistry();

    // Raw value types
    public static ValueTypeBoolean BOOLEAN = REGISTRY.register(new ValueTypeBoolean());
    public static ValueTypeInteger INTEGER = REGISTRY.register(new ValueTypeInteger());
    public static ValueTypeDouble  DOUBLE  = REGISTRY.register(new ValueTypeDouble());
    public static ValueTypeLong    LONG    = REGISTRY.register(new ValueTypeLong());
    public static ValueTypeString  STRING  = REGISTRY.register(new ValueTypeString());
    public static ValueTypeList    LIST    = REGISTRY.register(new ValueTypeList());

    // Object types
    public static ValueObjectTypeBlock      OBJECT_BLOCK      = REGISTRY.register(new ValueObjectTypeBlock());
    public static ValueObjectTypeItemStack  OBJECT_ITEMSTACK  = REGISTRY.register(new ValueObjectTypeItemStack());
    public static ValueObjectTypeEntity     OBJECT_ENTITY     = REGISTRY.register(new ValueObjectTypeEntity());
    public static ValueObjectTypeFluidStack OBJECT_FLUIDSTACK = REGISTRY.register(new ValueObjectTypeFluidStack());

    // Categories
    public static ValueTypeCategoryAny    CATEGORY_ANY    = REGISTRY.registerCategory(new ValueTypeCategoryAny());
    public static ValueTypeCategoryNumber CATEGORY_NUMBER = REGISTRY.registerCategory(new ValueTypeCategoryNumber());
    public static ValueTypeCategoryNamed  CATEGORY_NAMED  = REGISTRY.registerCategory(new ValueTypeCategoryNamed());

    private static IValueTypeRegistry constructRegistry() {
        // This also allows this registry to be used outside of a minecraft environment.
        if(MinecraftHelpers.isModdedEnvironment()) {
            return IntegratedDynamics._instance.getRegistryManager().getRegistry(IValueTypeRegistry.class);
        } else {
            return ValueTypeRegistry.getInstance();
        }
    }

    public static void load() {}

}
