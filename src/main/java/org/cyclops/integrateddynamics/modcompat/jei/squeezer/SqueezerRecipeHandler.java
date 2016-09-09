package org.cyclops.integrateddynamics.modcompat.jei.squeezer;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import org.cyclops.integrateddynamics.Reference;

import javax.annotation.Nonnull;

/**
 * Handler for the Squeezer recipes.
 * @author rubensworks
 */
public class SqueezerRecipeHandler implements IRecipeHandler<SqueezerRecipeJEI> {

    public static final String CATEGORY = Reference.MOD_ID + ":squeezer";

    @Nonnull
    @Override
    public Class<SqueezerRecipeJEI> getRecipeClass() {
        return SqueezerRecipeJEI.class;
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public String getRecipeCategoryUid() {
        return CATEGORY;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid(@Nonnull SqueezerRecipeJEI recipe) {
        return getRecipeCategoryUid();
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull SqueezerRecipeJEI recipe) {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(@Nonnull SqueezerRecipeJEI recipe) {
        return recipe != null;
    }

}
