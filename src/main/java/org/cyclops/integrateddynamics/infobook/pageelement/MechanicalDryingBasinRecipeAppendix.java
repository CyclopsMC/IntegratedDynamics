package org.cyclops.integrateddynamics.infobook.pageelement;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import org.cyclops.cyclopscore.infobook.IInfoBook;
import org.cyclops.cyclopscore.infobook.InfoBookParser;
import org.cyclops.integrateddynamics.RegistryEntries;

import java.util.function.Supplier;

/**
 * Mechanical drying basin recipes.
 * @author rubensworks
 */
public class MechanicalDryingBasinRecipeAppendix extends DryingBasinRecipeAppendix {
    public MechanicalDryingBasinRecipeAppendix(IInfoBook infoBook, Supplier<RecipeDisplayEntry> recipeDisplaySupplier) throws InfoBookParser.InvalidAppendixException {
        super(infoBook, recipeDisplaySupplier);
    }

    @Override
    protected String getUnlocalizedTitle() {
        return "block.integrateddynamics.mechanical_drying_basin";
    }

    protected ItemStack getCrafter() {
        return new ItemStack(RegistryEntries.BLOCK_MECHANICAL_DRYING_BASIN.get());
    }

}
