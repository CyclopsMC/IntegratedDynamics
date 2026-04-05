package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the drying basin recipe serializer.
 * @author rubensworks
 *
 */
public class RecipeSerializerDryingBasinConfig extends RecipeConfigCommon<RecipeDryingBasin, IntegratedDynamics> {

    public RecipeSerializerDryingBasinConfig() {
        super(
                IntegratedDynamics._instance,
                "drying_basin",
                eConfig -> RecipeSerializerDryingBasin.SERIALIZER
        );
    }

}
