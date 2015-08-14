package org.cyclops.integrateddynamics.part.aspect;

import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectRegistry;
import org.cyclops.integrateddynamics.part.aspect.read.inventory.*;
import org.cyclops.integrateddynamics.part.aspect.read.redstone.AspectReadBooleanRedstoneHigh;
import org.cyclops.integrateddynamics.part.aspect.read.redstone.AspectReadBooleanRedstoneLow;
import org.cyclops.integrateddynamics.part.aspect.read.redstone.AspectReadBooleanRedstoneNonLow;
import org.cyclops.integrateddynamics.part.aspect.read.redstone.AspectReadIntegerRedstone;
import org.cyclops.integrateddynamics.part.aspect.read.world.*;
import org.cyclops.integrateddynamics.part.aspect.write.AspectWriteBooleanRedstone;
import org.cyclops.integrateddynamics.part.aspect.write.AspectWriteIntegerRedstone;

/**
 * Collection of all aspects.
 * @author rubensworks
 */
public class Aspects {

    public static final IAspectRegistry REGISTRY = IntegratedDynamics._instance.getRegistryManager().getRegistry(IAspectRegistry.class);

    public static void load() {}

    // --------------- Read ---------------
    public static final AspectReadBooleanRedstoneLow READ_BOOLEAN_REDSTONE_LOW = new AspectReadBooleanRedstoneLow();
    public static final AspectReadBooleanRedstoneNonLow READ_BOOLEAN_REDSTONE_NONLOW = new AspectReadBooleanRedstoneNonLow();
    public static final AspectReadBooleanRedstoneHigh READ_BOOLEAN_REDSTONE_HIGH = new AspectReadBooleanRedstoneHigh();

    public static final AspectReadIntegerRedstone READ_INTEGER_REDSTONE = new AspectReadIntegerRedstone();

    public static final AspectReadBooleanInventoryFull READ_BOOLEAN_INVENTORY_FULL = new AspectReadBooleanInventoryFull();
    public static final AspectReadBooleanInventoryEmpty READ_BOOLEAN_INVENTORY_EMPTY = new AspectReadBooleanInventoryEmpty();
    public static final AspectReadBooleanInventoryNonEmpty READ_BOOLEAN_INVENTORY_NONEMPTY = new AspectReadBooleanInventoryNonEmpty();
    public static final AspectReadBooleanInventoryApplicable READ_BOOLEAN_INVENTORY_APPLICABLE = new AspectReadBooleanInventoryApplicable();

    public static final AspectReadIntegerInventory READ_INTEGER_INVENTORY_COUNT = new AspectReadIntegerInventory();

    public static final AspectReadBooleanWorldBlock READ_BOOLEAN_WORLD_BLOCK = new AspectReadBooleanWorldBlock();
    public static final AspectReadBooleanWorldEntity READ_BOOLEAN_WORLD_ENTITY = new AspectReadBooleanWorldEntity();
    public static final AspectReadBooleanWorldMob READ_BOOLEAN_WORLD_MOB = new AspectReadBooleanWorldMob();
    public static final AspectReadBooleanWorldPlayer READ_BOOLEAN_WORLD_PLAYER = new AspectReadBooleanWorldPlayer();
    public static final AspectReadBooleanWorldItem READ_BOOLEAN_WORLD_ITEM = new AspectReadBooleanWorldItem();

    public static final AspectReadIntegerWorldEntity READ_INTEGER_WORLD_ENTITY = new AspectReadIntegerWorldEntity();

    // --------------- Write ---------------
    public static final AspectWriteBooleanRedstone WRITE_BOOLEAN_REDSTONE = new AspectWriteBooleanRedstone();

    public static final AspectWriteIntegerRedstone WRITE_INTEGER_REDSTONE = new AspectWriteIntegerRedstone();

}
