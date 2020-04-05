package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the squeezer recipe serializer.
 * @author rubensworks
 *
 */
public class RecipeSerializerSqueezerConfig extends RecipeConfig<RecipeSqueezer> {

    public RecipeSerializerSqueezerConfig() {
        super(
                IntegratedDynamics._instance,
                "squeezer",
                eConfig -> new RecipeSerializerSqueezer()
        );
    }

}
