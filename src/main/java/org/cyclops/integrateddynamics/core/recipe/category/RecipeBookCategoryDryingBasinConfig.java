package org.cyclops.integrateddynamics.core.recipe.category;

import net.minecraft.world.item.crafting.RecipeBookCategory;
import org.cyclops.cyclopscore.config.extendedconfig.RecipeBookCategoryConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the drying basin recipe book category.
 * @author rubensworks
 *
 */
public class RecipeBookCategoryDryingBasinConfig extends RecipeBookCategoryConfigCommon<RecipeBookCategory, IntegratedDynamics> {

    public RecipeBookCategoryDryingBasinConfig() {
        super(
                IntegratedDynamics._instance,
                "drying_basin",
                createDefault()
        );
    }
}
