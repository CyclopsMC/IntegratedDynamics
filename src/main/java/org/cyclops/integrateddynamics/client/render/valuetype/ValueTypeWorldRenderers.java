package org.cyclops.integrateddynamics.client.render.valuetype;

import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * A collection of all value type world renderers.
 * @author rubensworks
 */
public class ValueTypeWorldRenderers {

    public static final IValueTypeWorldRendererRegistry REGISTRY = IntegratedDynamics._instance.getRegistryManager().getRegistry(IValueTypeWorldRendererRegistry.class);

    public static final TextValueTypeWorldRenderer DEFAULT = new TextValueTypeWorldRenderer();

    public static void load() {}

}
