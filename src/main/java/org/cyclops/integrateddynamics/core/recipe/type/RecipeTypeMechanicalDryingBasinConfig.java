package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.config.extendedconfig.RecipeTypeConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the mechanical drying basin recipe type.
 * @author rubensworks
 *
 */
public class RecipeTypeMechanicalDryingBasinConfig extends RecipeTypeConfig<RecipeMechanicalDryingBasin> {

    public RecipeTypeMechanicalDryingBasinConfig() {
        super(
                IntegratedDynamics._instance,
                "mechanical_drying_basin"
        );
        RegistryEntries.RECIPETYPE_MECHANICAL_DRYING_BASIN = getInstance();
    }

}
