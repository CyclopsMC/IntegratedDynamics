package org.cyclops.integrateddynamics.core.recipe.type;


import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;


/**
 * Config for the drying basin recipe serializer.
 * @author rubensworks
 *
 */
public class RecipeSerializerFacadeSqueezeConfig extends RecipeConfig<RecipeFacadeSqueeze> {

    public RecipeSerializerFacadeSqueezeConfig() {
        super(
                IntegratedDynamics._instance,
                "facade_squeeze",
                eConfig -> new RecipeSerializerFacadeSqueeze()
        );
    }

}
