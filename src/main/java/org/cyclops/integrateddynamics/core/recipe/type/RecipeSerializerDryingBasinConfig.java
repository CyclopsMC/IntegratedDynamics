package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the drying basin recipe serializer.
 * @author rubensworks
 *
 */
public class RecipeSerializerDryingBasinConfig extends RecipeConfig<RecipeDryingBasin> {

    public RecipeSerializerDryingBasinConfig() {
        super(
                IntegratedDynamics._instance,
                "drying_basin",
                eConfig -> new RecipeSerializerDryingBasin()
        );
    }

}
