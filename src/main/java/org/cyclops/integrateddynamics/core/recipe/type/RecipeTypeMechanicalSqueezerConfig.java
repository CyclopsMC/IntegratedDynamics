package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.config.extendedconfig.RecipeTypeConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the mechanical squeezer recipe type.
 * @author rubensworks
 *
 */
public class RecipeTypeMechanicalSqueezerConfig extends RecipeTypeConfigCommon<RecipeMechanicalSqueezer, IntegratedDynamics> {

    public RecipeTypeMechanicalSqueezerConfig() {
        super(
                IntegratedDynamics._instance,
                "mechanical_squeezer"
        );
    }
}
