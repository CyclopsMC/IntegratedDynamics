package org.cyclops.integrateddynamics.client.render.valuetype;

import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRendererRegistry;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

/**
 * A collection of all value type world renderers.
 * @author rubensworks
 */
public class ValueTypeWorldRenderers {

    public static final IValueTypeWorldRendererRegistry REGISTRY = IntegratedDynamics._instance.getRegistryManager().getRegistry(IValueTypeWorldRendererRegistry.class);

    public static final TextValueTypeWorldRenderer DEFAULT = new TextValueTypeWorldRenderer();

    public static void load() {
        REGISTRY.register(ValueTypes.LIST, new ListValueTypeWorldRenderer());
    }

}
