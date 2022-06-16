package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.config.extendedconfig.RecipeTypeConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the drying basin recipe type.
 * @author rubensworks
 *
 */
public class RecipeTypeDryingBasinConfig extends RecipeTypeConfig<RecipeDryingBasin> {

    public RecipeTypeDryingBasinConfig() {
        super(
                IntegratedDynamics._instance,
                "drying_basin"
        );
    }
}
