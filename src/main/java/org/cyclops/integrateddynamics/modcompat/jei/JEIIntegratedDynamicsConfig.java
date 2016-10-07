package org.cyclops.integrateddynamics.modcompat.jei;

import mezz.jei.api.*;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import net.minecraft.item.ItemStack;
import org.cyclops.integrateddynamics.block.BlockDryingBasin;
import org.cyclops.integrateddynamics.block.BlockSqueezer;
import org.cyclops.integrateddynamics.modcompat.jei.dryingbasin.DryingBasinRecipeCategory;
import org.cyclops.integrateddynamics.modcompat.jei.dryingbasin.DryingBasinRecipeHandler;
import org.cyclops.integrateddynamics.modcompat.jei.dryingbasin.DryingBasinRecipeJEI;
import org.cyclops.integrateddynamics.modcompat.jei.squeezer.SqueezerRecipeCategory;
import org.cyclops.integrateddynamics.modcompat.jei.squeezer.SqueezerRecipeHandler;
import org.cyclops.integrateddynamics.modcompat.jei.squeezer.SqueezerRecipeJEI;

import javax.annotation.Nonnull;

/**
 * Helper for registering JEI manager.
 * @author rubensworks
 *
 */
@JEIPlugin
public class JEIIntegratedDynamicsConfig implements IModPlugin {

    public static IJeiHelpers JEI_HELPER;

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {

    }

    @Override
    public void register(@Nonnull IModRegistry registry) {
        if(JEIModCompat.canBeUsed) {
            JEI_HELPER = registry.getJeiHelpers();
            // Drying Basin
            registry.addRecipes(DryingBasinRecipeJEI.getAllRecipes());
            registry.addRecipeCategories(new DryingBasinRecipeCategory(JEI_HELPER.getGuiHelper()));
            registry.addRecipeHandlers(new DryingBasinRecipeHandler());
            registry.addRecipeCategoryCraftingItem(new ItemStack(BlockDryingBasin.getInstance()), DryingBasinRecipeHandler.CATEGORY);

            // Squeezer
            registry.addRecipes(SqueezerRecipeJEI.getAllRecipes());
            registry.addRecipeCategories(new SqueezerRecipeCategory(JEI_HELPER.getGuiHelper()));
            registry.addRecipeHandlers(new SqueezerRecipeHandler());
            registry.addRecipeCategoryCraftingItem(new ItemStack(BlockSqueezer.getInstance()), SqueezerRecipeHandler.CATEGORY);
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {

    }
}
