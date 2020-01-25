package org.cyclops.integrateddynamics.infobook.pageelement;

import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.infobook.IInfoBook;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientAndFluidStackRecipeComponent;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Mechanical drying basin recipes.
 * @author rubensworks
 */
public class MechanicalDryingBasinRecipeAppendix extends DryingBasinRecipeAppendix {
    public MechanicalDryingBasinRecipeAppendix(IInfoBook infoBook, IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> recipe) {
        super(infoBook, recipe);
    }

    @Override
    protected String getUnlocalizedTitle() {
        return "tile.blocks.integrateddynamics.mechanical_drying_basin";
    }

    protected ItemStack getCrafter() {
        return new ItemStack(RegistryEntries.BLOCK_MECHANICAL_DRYING_BASIN);
    }

}
