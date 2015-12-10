package org.cyclops.integrateddynamics.client.render.part;

import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.client.render.part.IPartOverlayRendererRegistry;
import org.cyclops.integrateddynamics.core.part.PartTypes;

/**
 * A collection of all part overlay renderers
 * @author rubensworks
 */
public class PartOverlayRenderers {

    public static final IPartOverlayRendererRegistry REGISTRY = IntegratedDynamics._instance.getRegistryManager().getRegistry(IPartOverlayRendererRegistry.class);

    public static final DisplayPartOverlayRenderer DISPLAY = REGISTRY.register(PartTypes.DISPLAY_PANEL, new DisplayPartOverlayRenderer());

    public static void load() {}

}
