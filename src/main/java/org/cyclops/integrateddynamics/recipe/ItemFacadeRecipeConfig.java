package org.cyclops.integrateddynamics.recipe;

import net.minecraft.world.item.crafting.CustomRecipe;
import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link ItemFacadeRecipe}.
 * @author rubensworks
 */
public class ItemFacadeRecipeConfig extends RecipeConfigCommon<ItemFacadeRecipe, IntegratedDynamics> {

    public ItemFacadeRecipeConfig() {
        super(IntegratedDynamics._instance,
                "crafting_special_facade",
                eConfig -> new CustomRecipe.Serializer<>(ItemFacadeRecipe::new));
    }

}
