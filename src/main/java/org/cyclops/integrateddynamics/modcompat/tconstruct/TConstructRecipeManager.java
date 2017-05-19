package org.cyclops.integrateddynamics.modcompat.tconstruct;

import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.ItemAndFluidStackRecipeComponent;
import org.cyclops.integrateddynamics.block.BlockCrystalizedMenrilBlockConfig;
import org.cyclops.integrateddynamics.block.BlockDryingBasin;
import slimeknights.tconstruct.library.TinkerRegistry;

/**
 * @author rubensworks
 */
public class TConstructRecipeManager {

    public static void register() {
        IRecipe<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties> recipe = BlockDryingBasin.getInstance().getRecipeRegistry().findRecipeByOutput(
                new ItemAndFluidStackRecipeComponent(
                        new ItemStack(BlockCrystalizedMenrilBlockConfig._instance.getBlockInstance()), null));
        if (recipe != null) {
            TinkerRegistry.registerBasinCasting(recipe.getOutput().getItemStack(), ItemStack.EMPTY,
                    recipe.getInput().getFluidStack().getFluid(), recipe.getInput().getFluidStack().amount);
        }
    }

}
