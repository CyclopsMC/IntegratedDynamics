package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the squeezer recipe serializer.
 * @author rubensworks
 *
 */
public class RecipeSerializerSqueezerConfig extends RecipeConfigCommon<RecipeSqueezer, IntegratedDynamics> {

    public RecipeSerializerSqueezerConfig() {
        super(
                IntegratedDynamics._instance,
                "squeezer",
                eConfig -> RecipeSerializerSqueezer.SERIALIZER
        );
    }

}
