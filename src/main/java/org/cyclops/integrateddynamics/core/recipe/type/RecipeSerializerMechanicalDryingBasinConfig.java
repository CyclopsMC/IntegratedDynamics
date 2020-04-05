package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the mechanical drying basin recipe serializer.
 * @author rubensworks
 *
 */
public class RecipeSerializerMechanicalDryingBasinConfig extends RecipeConfig<RecipeMechanicalDryingBasin> {

    public RecipeSerializerMechanicalDryingBasinConfig() {
        super(
                IntegratedDynamics._instance,
                "mechanical_drying_basin",
                eConfig -> new RecipeSerializerMechanicalDryingBasin()
        );
    }

}
