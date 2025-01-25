package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.config.extendedconfig.RecipeTypeConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the squeezer recipe type.
 * @author rubensworks
 *
 */
public class RecipeTypeSqueezerConfig extends RecipeTypeConfigCommon<RecipeSqueezer, IntegratedDynamics> {

    public RecipeTypeSqueezerConfig() {
        super(
                IntegratedDynamics._instance,
                "squeezer"
        );
    }
}
