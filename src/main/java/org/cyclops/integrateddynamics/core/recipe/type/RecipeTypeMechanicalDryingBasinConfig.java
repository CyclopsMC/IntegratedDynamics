package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.config.extendedconfig.RecipeTypeConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the mechanical drying basin recipe type.
 * @author rubensworks
 *
 */
public class RecipeTypeMechanicalDryingBasinConfig extends RecipeTypeConfigCommon<RecipeMechanicalDryingBasin, IntegratedDynamics> {

    public RecipeTypeMechanicalDryingBasinConfig() {
        super(
                IntegratedDynamics._instance,
                "mechanical_drying_basin"
        );
    }
}
