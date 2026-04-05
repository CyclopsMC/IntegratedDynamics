package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;


/**
 * Config for the drying Facade Squeeze Mechanical serializer.
 * @author kirjorjos
 *
 */
public class RecipeSerializerMechanicalSqueezerFacadeConfig extends RecipeConfigCommon<RecipeMechanicalSqueezerFacade, IntegratedDynamics> {

    public RecipeSerializerMechanicalSqueezerFacadeConfig() {
        super(
                IntegratedDynamics._instance,
                "mechanical_squeezer_facade",
                eConfig -> RecipeSerializerMechanicalSqueezerFacade.SERIALIZER
        );
    }

}
