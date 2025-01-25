package org.cyclops.integrateddynamics.core.recipe.category;

import net.minecraft.world.item.crafting.RecipeBookCategory;
import org.cyclops.cyclopscore.config.extendedconfig.RecipeBookCategoryConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the squeezer recipe book category.
 * @author rubensworks
 *
 */
public class RecipeBookCategorySqueezerConfig extends RecipeBookCategoryConfigCommon<RecipeBookCategory, IntegratedDynamics> {

    public RecipeBookCategorySqueezerConfig() {
        super(
                IntegratedDynamics._instance,
                "squeezer",
                createDefault()
        );
    }
}
