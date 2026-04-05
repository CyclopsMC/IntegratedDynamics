package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the mechanical squeezer recipe serializer.
 * @author rubensworks
 *
 */
public class RecipeSerializerMechanicalSqueezerConfig extends RecipeConfigCommon<RecipeMechanicalSqueezer, IntegratedDynamics> {

    public RecipeSerializerMechanicalSqueezerConfig() {
        super(
                IntegratedDynamics._instance,
                "mechanical_squeezer",
                eConfig -> RecipeSerializerMechanicalSqueezer.SERIALIZER
        );
    }

}
