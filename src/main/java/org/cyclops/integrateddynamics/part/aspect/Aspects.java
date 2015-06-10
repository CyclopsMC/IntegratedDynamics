package org.cyclops.integrateddynamics.part.aspect;

import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectRegistry;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBooleanRedstoneHigh;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBooleanRedstoneLow;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBooleanRedstoneNonLow;
import org.cyclops.integrateddynamics.part.aspect.write.AspectWriteBooleanRedstone;

/**
 * Collection of all aspects.
 * @author rubensworks
 */
public class Aspects {

    public static final IAspectRegistry REGISTRY = IntegratedDynamics._instance.getRegistryManager().getRegistry(IAspectRegistry.class);

    // --------------- Read ---------------
    public static final AspectReadBooleanRedstoneLow READ_BOOLEAN_REDSTONE_LOW = new AspectReadBooleanRedstoneLow();
    public static final AspectReadBooleanRedstoneNonLow READ_BOOLEAN_REDSTONE_NONLOW = new AspectReadBooleanRedstoneNonLow();
    public static final AspectReadBooleanRedstoneHigh READ_BOOLEAN_REDSTONE_HIGH = new AspectReadBooleanRedstoneHigh();

    // --------------- Write ---------------
    public static final AspectWriteBooleanRedstone WRITE_BOOLEAN_REDSTONE = new AspectWriteBooleanRedstone();

}
