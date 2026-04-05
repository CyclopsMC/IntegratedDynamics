package org.cyclops.integrateddynamics.core.recipe.type;

import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link RecipeEnergyContainerCombination}.
 * @author rubensworks
 */
public class RecipeEnergyContainerCombinationConfig extends RecipeConfigCommon<RecipeEnergyContainerCombination, IntegratedDynamics> {

    public RecipeEnergyContainerCombinationConfig() {
        super(IntegratedDynamics._instance,
                String.format("crafting_special_energycontainer_combination"),
                eConfig -> RecipeSerializerEnergyContainerCombination.SERIALIZER);
    }

}
