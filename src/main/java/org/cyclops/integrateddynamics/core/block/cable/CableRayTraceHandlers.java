package org.cyclops.integrateddynamics.core.block.cable;

import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.block.cable.ICableRayTraceHandlerRegistry;

/**
 * @author rubensworks
 */
public class CableRayTraceHandlers {

    public static final ICableRayTraceHandlerRegistry REGISTRY = IntegratedDynamics._instance.getRegistryManager().getRegistry(ICableRayTraceHandlerRegistry.class);

}
