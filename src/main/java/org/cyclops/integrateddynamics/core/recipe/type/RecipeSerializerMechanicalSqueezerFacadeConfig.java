package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;


/**
 * Config for the drying Facade Squeeze Mechanical serializer.
 * @author kirjorjos
 *
 */
public class RecipeSerializerMechanicalSqueezerFacadeConfig extends RecipeConfig<RecipeMechanicalSqueezerFacade> {

    public RecipeSerializerMechanicalSqueezerFacadeConfig() {
        super(
                IntegratedDynamics._instance,
                "mechanical_squeezer_facade",
                eConfig -> new RecipeSerializerMechanicalSqueezerFacade()
        );
    }

}
