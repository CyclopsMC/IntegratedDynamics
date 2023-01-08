package org.cyclops.integrateddynamics.recipe;

import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for {@link ItemFacadeRecipe}.
 * @author rubensworks
 */
public class ItemFacadeRecipeConfig extends RecipeConfig<ItemFacadeRecipe> {

    public ItemFacadeRecipeConfig() {
        super(IntegratedDynamics._instance,
                "crafting_special_facade",
                eConfig -> new SimpleCraftingRecipeSerializer<>(ItemFacadeRecipe::new));
    }

}
