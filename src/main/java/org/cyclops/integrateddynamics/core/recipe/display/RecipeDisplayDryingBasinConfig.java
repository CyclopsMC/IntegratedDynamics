package org.cyclops.integrateddynamics.core.recipe.display;

import org.cyclops.cyclopscore.config.extendedconfig.RecipeDisplayConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the drying basin recipe display.
 * @author rubensworks
 *
 */
public class RecipeDisplayDryingBasinConfig extends RecipeDisplayConfigCommon<RecipeDisplayDryingBasin, IntegratedDynamics> {

    public RecipeDisplayDryingBasinConfig() {
        super(
                IntegratedDynamics._instance,
                "drying_basin",
                eConfig -> RecipeDisplayDryingBasin.TYPE
        );
    }
}
