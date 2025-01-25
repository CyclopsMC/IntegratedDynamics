package org.cyclops.integrateddynamics.core.recipe.display;

import org.cyclops.cyclopscore.config.extendedconfig.RecipeDisplayConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the squeezer recipe display.
 * @author rubensworks
 *
 */
public class RecipeDisplaySqueezerConfig extends RecipeDisplayConfigCommon<RecipeDisplaySqueezer, IntegratedDynamics> {

    public RecipeDisplaySqueezerConfig() {
        super(
                IntegratedDynamics._instance,
                "squeezer",
                eConfig -> RecipeDisplaySqueezer.TYPE
        );
    }
}
