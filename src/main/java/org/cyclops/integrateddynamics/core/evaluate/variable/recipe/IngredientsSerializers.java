package org.cyclops.integrateddynamics.core.evaluate.variable.recipe;

import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.recipe.IIngredientsSerializerRegistry;

/**
 * Collection of available operators.
 *
 * @author rubensworks
 */
public final class IngredientsSerializers {

    public static final IIngredientsSerializerRegistry REGISTRY = constructRegistry();

    private static IIngredientsSerializerRegistry constructRegistry() {
        // This also allows this registry to be used outside of a minecraft environment.
        if(MinecraftHelpers.isModdedEnvironment()) {
            return IntegratedDynamics._instance.getRegistryManager().getRegistry(IIngredientsSerializerRegistry.class);
        } else {
            return IngredientsSerializerRegistry.getInstance();
        }
    }

    public static void load() {
        REGISTRY.registerSerializer(new IngredientsRecipeItemMatch.Serializer());
    }

}
