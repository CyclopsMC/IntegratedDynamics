package org.cyclops.integrateddynamics.api.block.cable;

import org.cyclops.cyclopscore.init.IRegistry;

import java.util.Collection;

/**
 * @author rubensworks
 */
public interface ICableRayTraceHandlerRegistry extends IRegistry {

    public ICableRayTraceHandler register(ICableRayTraceHandler handler);

    public Collection<ICableRayTraceHandler> getHandlers();

}
