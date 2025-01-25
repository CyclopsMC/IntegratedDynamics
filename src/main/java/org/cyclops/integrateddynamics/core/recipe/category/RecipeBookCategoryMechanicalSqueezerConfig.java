package org.cyclops.integrateddynamics.core.recipe.category;

import net.minecraft.world.item.crafting.RecipeBookCategory;
import org.cyclops.cyclopscore.config.extendedconfig.RecipeBookCategoryConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the mechanical squeezer recipe book category.
 * @author rubensworks
 *
 */
public class RecipeBookCategoryMechanicalSqueezerConfig extends RecipeBookCategoryConfigCommon<RecipeBookCategory, IntegratedDynamics> {

    public RecipeBookCategoryMechanicalSqueezerConfig() {
        super(
                IntegratedDynamics._instance,
                "mechanical_squeezer",
                createDefault()
        );
    }
}
