package org.cyclops.integrateddynamics.recipe;

import net.minecraft.item.crafting.SpecialRecipeSerializer;
import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link ItemVariableCopyRecipe}.
 * @author rubensworks
 */
public class ItemVariableCopyRecipeConfig extends RecipeConfig<ItemVariableCopyRecipe> {

    public ItemVariableCopyRecipeConfig() {
        super(IntegratedDynamics._instance,
                "crafting_special_variable_copy",
                eConfig -> new SpecialRecipeSerializer<>(ItemVariableCopyRecipe::new));
    }

}
