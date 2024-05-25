package org.cyclops.integrateddynamics.infobook.pageelement;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.cyclops.cyclopscore.infobook.IInfoBook;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeMechanicalSqueezer;

/**
 * Mechanical squeezer recipes.
 * @author rubensworks
 */
public class MechanicalSqueezerRecipeAppendix extends SqueezerRecipeAppendix {
    public MechanicalSqueezerRecipeAppendix(IInfoBook infoBook, RecipeHolder<RecipeMechanicalSqueezer> recipe) {
        super(infoBook, recipe);
    }

    @Override
    protected String getUnlocalizedTitle() {
        return "block.integrateddynamics.mechanical_squeezer";
    }

    @Override
    protected ItemStack getCrafter() {
        return new ItemStack(RegistryEntries.BLOCK_MECHANICAL_SQUEEZER.get());
    }
}
