package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.config.extendedconfig.RecipeTypeConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the mechanical squeezer recipe type.
 * @author rubensworks
 *
 */
public class RecipeTypeMechanicalSqueezerConfig extends RecipeTypeConfig<RecipeMechanicalSqueezer> {

    public RecipeTypeMechanicalSqueezerConfig() {
        super(
                IntegratedDynamics._instance,
                "mechanical_squeezer"
        );
    }

    @Override
    public void onRegistered() {
        super.onRegistered();

        RegistryEntries.RECIPETYPE_MECHANICAL_SQUEEZER = getInstance();
    }
}
