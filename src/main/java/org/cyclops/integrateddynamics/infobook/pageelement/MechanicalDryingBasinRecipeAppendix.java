package org.cyclops.integrateddynamics.infobook.pageelement;

import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.infobook.IInfoBook;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeMechanicalDryingBasin;

/**
 * Mechanical drying basin recipes.
 * @author rubensworks
 */
public class MechanicalDryingBasinRecipeAppendix extends DryingBasinRecipeAppendix {
    public MechanicalDryingBasinRecipeAppendix(IInfoBook infoBook, RecipeMechanicalDryingBasin recipe) {
        super(infoBook, recipe);
    }

    @Override
    protected String getUnlocalizedTitle() {
        return "block.integrateddynamics.mechanical_drying_basin";
    }

    protected ItemStack getCrafter() {
        return new ItemStack(RegistryEntries.BLOCK_MECHANICAL_DRYING_BASIN);
    }

}
