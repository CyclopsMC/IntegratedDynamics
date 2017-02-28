package org.cyclops.integrateddynamics.modcompat.immersiveengineering;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.component.DummyPropertiesComponent;
import org.cyclops.cyclopscore.recipe.custom.component.ItemAndFluidStackRecipeComponent;
import org.cyclops.cyclopscore.recipe.custom.component.ItemStackRecipeComponent;
import org.cyclops.integrateddynamics.block.BlockSqueezer;
import org.cyclops.integrateddynamics.fluid.FluidMenrilResin;

import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;

/**
 * Immersive Engineering recipe manager registrations.
 * @author runesmacher
 *
 */
public class ImmersiveEngineeringRecipeManager {
    public static void register() {
        IRecipe<ItemStackRecipeComponent, ItemAndFluidStackRecipeComponent, DummyPropertiesComponent> recipe = BlockSqueezer.getInstance().getRecipeRegistry().findRecipeByOutput(
                new ItemAndFluidStackRecipeComponent(
                        (ItemStack) null, new FluidStack(FluidMenrilResin.getInstance(), Fluid.BUCKET_VOLUME)));

        // Register Menril Resin squeezer recipe.
        if(recipe != null) {
            int energy = 10000;
            ItemStack[] input = {recipe.getInput().getItemStack()};
            FluidStack fluidStack = recipe.getOutput().getFluidStack();
            SqueezerRecipe squeezerRecipe = new SqueezerRecipe(fluidStack, null, input, energy);
            SqueezerRecipe.recipeList.add(squeezerRecipe);
        }
    }

}
