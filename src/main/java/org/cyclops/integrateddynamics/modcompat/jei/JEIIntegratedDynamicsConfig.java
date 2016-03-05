package org.cyclops.integrateddynamics.modcompat.jei;

import mezz.jei.api.*;
import org.cyclops.integrateddynamics.modcompat.jei.dryingbasin.DryingBasinRecipeCategory;
import org.cyclops.integrateddynamics.modcompat.jei.dryingbasin.DryingBasinRecipeHandler;
import org.cyclops.integrateddynamics.modcompat.jei.dryingbasin.DryingBasinRecipeJEI;
import org.cyclops.integrateddynamics.modcompat.jei.squeezer.SqueezerRecipeCategory;
import org.cyclops.integrateddynamics.modcompat.jei.squeezer.SqueezerRecipeHandler;
import org.cyclops.integrateddynamics.modcompat.jei.squeezer.SqueezerRecipeJEI;

/**
 * Helper for registering JEI manager.
 * @author rubensworks
 *
 */
@JEIPlugin
public class JEIIntegratedDynamicsConfig implements IModPlugin {

    public static IJeiHelpers JEI_HELPER;

    @Override
    public void onJeiHelpersAvailable(IJeiHelpers jeiHelpers) {
        JEI_HELPER = jeiHelpers;
    }

    @Override
    public void onItemRegistryAvailable(IItemRegistry itemRegistry) {

    }

    @Override
    public void register(IModRegistry registry) {
        if(JEIModCompat.canBeUsed) {
            // Drying Basin
            registry.addRecipes(DryingBasinRecipeJEI.getAllRecipes());
            registry.addRecipeCategories(new DryingBasinRecipeCategory(JEI_HELPER.getGuiHelper()));
            registry.addRecipeHandlers(new DryingBasinRecipeHandler());

            // Squeezer
            registry.addRecipes(SqueezerRecipeJEI.getAllRecipes());
            registry.addRecipeCategories(new SqueezerRecipeCategory(JEI_HELPER.getGuiHelper()));
            registry.addRecipeHandlers(new SqueezerRecipeHandler());
        }
    }

    @Override
    public void onRecipeRegistryAvailable(IRecipeRegistry recipeRegistry) {

    }
}
