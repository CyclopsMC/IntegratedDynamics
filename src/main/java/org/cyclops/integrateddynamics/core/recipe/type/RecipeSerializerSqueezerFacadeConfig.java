package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;


/**
 * Config for the Facade Squeeze recipe serializer.
 * @author kirjorjos
 *
 */
public class RecipeSerializerSqueezerFacadeConfig extends RecipeConfigCommon<RecipeSqueezerFacade, IntegratedDynamics> {

    public RecipeSerializerSqueezerFacadeConfig() {
        super(
                IntegratedDynamics._instance,
                "squeezer_facade",
                eConfig -> RecipeSerializerSqueezerFacade.SERIALIZER
        );
    }

}
