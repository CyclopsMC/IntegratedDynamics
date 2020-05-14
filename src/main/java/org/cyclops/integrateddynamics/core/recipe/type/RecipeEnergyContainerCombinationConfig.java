package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link RecipeEnergyContainerCombination}.
 * @author rubensworks
 */
public class RecipeEnergyContainerCombinationConfig extends RecipeConfig<RecipeEnergyContainerCombination> {

    public RecipeEnergyContainerCombinationConfig() {
        super(IntegratedDynamics._instance,
                String.format("crafting_special_energycontainer_combination"),
                eConfig -> new RecipeSerializerEnergyContainerCombination());
    }

}
