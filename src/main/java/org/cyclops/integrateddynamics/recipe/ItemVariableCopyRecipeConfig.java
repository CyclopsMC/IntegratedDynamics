package org.cyclops.integrateddynamics.recipe;

import net.minecraft.world.item.crafting.CustomRecipe;
import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link ItemVariableCopyRecipe}.
 * @author rubensworks
 */
public class ItemVariableCopyRecipeConfig extends RecipeConfigCommon<ItemVariableCopyRecipe, IntegratedDynamics> {

    public ItemVariableCopyRecipeConfig() {
        super(IntegratedDynamics._instance,
                "crafting_special_variable_copy",
                eConfig -> new CustomRecipe.Serializer<>(ItemVariableCopyRecipe::new));
    }

}
