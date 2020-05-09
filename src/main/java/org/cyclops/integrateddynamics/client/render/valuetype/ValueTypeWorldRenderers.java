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
        REGISTRY.register(ValueTypes.OBJECT_ITEMSTACK, new ItemValueTypeWorldRenderer());
        REGISTRY.register(ValueTypes.OBJECT_BLOCK, new BlockValueTypeWorldRenderer());
        REGISTRY.register(ValueTypes.OBJECT_FLUIDSTACK, new FluidValueTypeWorldRenderer());
        REGISTRY.register(ValueTypes.LIST, new ListValueTypeWorldRenderer());
        REGISTRY.register(ValueTypes.NBT, new NbtValueTypeWorldRenderer());
        REGISTRY.register(ValueTypes.OPERATOR, new OperatorValueTypeWorldRenderer());
        REGISTRY.register(ValueTypes.OBJECT_INGREDIENTS, new IngredientsValueTypeWorldRenderer());
        REGISTRY.register(ValueTypes.OBJECT_RECIPE, new RecipeValueTypeWorldRenderer());
    }

}
