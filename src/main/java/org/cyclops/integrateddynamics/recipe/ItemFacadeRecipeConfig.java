package org.cyclops.integrateddynamics.recipe;

import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
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
                eConfig -> new SimpleRecipeSerializer<>(ItemFacadeRecipe::new));
    }

}
