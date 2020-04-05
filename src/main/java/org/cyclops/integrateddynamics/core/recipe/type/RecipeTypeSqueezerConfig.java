package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.config.extendedconfig.RecipeTypeConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the squeezer recipe type.
 * @author rubensworks
 *
 */
public class RecipeTypeSqueezerConfig extends RecipeTypeConfig<RecipeSqueezer> {

    public RecipeTypeSqueezerConfig() {
        super(
                IntegratedDynamics._instance,
                "squeezer"
        );
        RegistryEntries.RECIPETYPE_SQUEEZER = getInstance();
    }

}
