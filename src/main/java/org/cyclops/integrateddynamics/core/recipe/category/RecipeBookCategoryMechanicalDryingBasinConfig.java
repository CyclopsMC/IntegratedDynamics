package org.cyclops.integrateddynamics.core.recipe.category;

import net.minecraft.world.item.crafting.RecipeBookCategory;
import org.cyclops.cyclopscore.config.extendedconfig.RecipeBookCategoryConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the mechanical drying basin recipe book category.
 * @author rubensworks
 *
 */
public class RecipeBookCategoryMechanicalDryingBasinConfig extends RecipeBookCategoryConfigCommon<RecipeBookCategory, IntegratedDynamics> {

    public RecipeBookCategoryMechanicalDryingBasinConfig() {
        super(
                IntegratedDynamics._instance,
                "mechanical_drying_basin",
                createDefault()
        );
    }
}
