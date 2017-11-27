package org.cyclops.integrateddynamics.infobook.pageelement;

import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.infobook.IInfoBook;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientRecipeComponent;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientsAndFluidStackRecipeComponent;
import org.cyclops.integrateddynamics.block.BlockMechanicalSqueezer;

/**
 * Mechanical squeezer recipes.
 * @author rubensworks
 */
public class MechanicalSqueezerRecipeAppendix extends SqueezerRecipeAppendix {
    public MechanicalSqueezerRecipeAppendix(IInfoBook infoBook, IRecipe<IngredientRecipeComponent, IngredientsAndFluidStackRecipeComponent, ?> recipe) {
        super(infoBook, recipe);
    }

    @Override
    protected String getUnlocalizedTitle() {
        return "tile.blocks.integrateddynamics.mechanical_squeezer.name";
    }

    @Override
    protected ItemStack getCrafter() {
        return new ItemStack(BlockMechanicalSqueezer.getInstance());
    }
}
