package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the mechanical drying basin recipe serializer.
 * @author rubensworks
 *
 */
public class RecipeSerializerMechanicalDryingBasinConfig extends RecipeConfigCommon<RecipeMechanicalDryingBasin, IntegratedDynamics> {

    public RecipeSerializerMechanicalDryingBasinConfig() {
        super(
                IntegratedDynamics._instance,
                "mechanical_drying_basin",
                eConfig -> RecipeSerializerMechanicalDryingBasin.SERIALIZER
        );
    }

}
