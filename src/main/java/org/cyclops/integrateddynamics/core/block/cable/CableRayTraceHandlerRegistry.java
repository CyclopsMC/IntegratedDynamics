package org.cyclops.integrateddynamics.core.block.cable;

import org.cyclops.integrateddynamics.api.block.cable.ICableRayTraceHandler;
import org.cyclops.integrateddynamics.api.block.cable.ICableRayTraceHandlerRegistry;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author rubensworks
 */
public class CableRayTraceHandlerRegistry implements ICableRayTraceHandlerRegistry {

    private static final CableRayTraceHandlerRegistry INSTANCE = new CableRayTraceHandlerRegistry();

    private final Set<ICableRayTraceHandler> handlers = new HashSet<>();

    /**
     * @return The unique instance.
     */
    public static CableRayTraceHandlerRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public ICableRayTraceHandler register(ICableRayTraceHandler handler) {
        handlers.add(handler);
        return handler;
    }

    @Override
    public Collection<ICableRayTraceHandler> getHandlers() {
        return handlers;
    }
}
