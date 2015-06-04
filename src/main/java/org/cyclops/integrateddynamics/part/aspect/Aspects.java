package org.cyclops.integrateddynamics.part.aspect;

import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectRegistry;
import org.cyclops.integrateddynamics.part.aspect.read.AspectBooleanRedstoneHigh;
import org.cyclops.integrateddynamics.part.aspect.read.AspectBooleanRedstoneLow;
import org.cyclops.integrateddynamics.part.aspect.read.AspectBooleanRedstoneNonLow;

/**
 * Collection of all aspects.
 * @author rubensworks
 */
public class Aspects {

    public static final IAspectRegistry REGISTRY = IntegratedDynamics._instance.getRegistryManager().getRegistry(IAspectRegistry.class);

    public static final AspectBooleanRedstoneLow READ_BOOLEAN_REDSTONE_LOW = new AspectBooleanRedstoneLow();
    public static final AspectBooleanRedstoneNonLow READ_BOOLEAN_REDSTONE_NONLOW = new AspectBooleanRedstoneNonLow();
    public static final AspectBooleanRedstoneHigh READ_BOOLEAN_REDSTONE_HIGH = new AspectBooleanRedstoneHigh();

}
