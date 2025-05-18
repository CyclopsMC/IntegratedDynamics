package org.cyclops.integrateddynamics.core.recipe.type;


import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;


/**
 * Config for the Squeeze Facade recipe serializer.
 * @author kirjorjos
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
