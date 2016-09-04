package org.cyclops.integrateddynamics.modcompat.jei.dryingbasin;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import org.cyclops.integrateddynamics.Reference;

import javax.annotation.Nonnull;

/**
 * Handler for the Drying Basin recipes.
 * @author rubensworks
 */
public class DryingBasinRecipeHandler implements IRecipeHandler<DryingBasinRecipeJEI> {

    public static final String CATEGORY = Reference.MOD_ID + ":dryingBasin";

    @Nonnull
    @Override
    public Class<DryingBasinRecipeJEI> getRecipeClass() {
        return DryingBasinRecipeJEI.class;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid() {
        return CATEGORY;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid(@Nonnull DryingBasinRecipeJEI recipe) {
        return getRecipeCategoryUid();
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull DryingBasinRecipeJEI recipe) {
        return recipe;
    }

    @Override
    public boolean isRecipeValid(@Nonnull DryingBasinRecipeJEI recipe) {
        return recipe != null;
    }

}
