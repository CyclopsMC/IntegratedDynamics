package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;


/**
 * Config for the Facade Squeeze recipe serializer.
 * @author kirjorjos
 *
 */
public class RecipeSerializerSqueezerFacadeConfig extends RecipeConfig<RecipeSqueezerFacade> {

    public RecipeSerializerSqueezerFacadeConfig() {
        super(
                IntegratedDynamics._instance,
                "squeezer_facade",
                eConfig -> new RecipeSerializerSqueezerFacade()
        );
    }

}
