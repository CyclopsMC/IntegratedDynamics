package org.cyclops.integrateddynamics.modcompat.tconstruct;

import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.modcompat.IModCompat;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.ItemAndFluidStackRecipeComponent;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.block.BlockCrystalizedMenrilBlockConfig;
import org.cyclops.integrateddynamics.block.BlockDryingBasin;
import slimeknights.tconstruct.library.TinkerRegistry;

/**
 * Compatibility plugin for Tinkers' Construct.
 * @author rubensworks
 *
 */
public class TConstructModCompat implements IModCompat {

    @Override
    public String getModID() {
       return Reference.MOD_TCONSTRUCT;
    }

    @Override
    public void onInit(Step step) {
    	if(step == Step.POSTINIT) {
            IRecipe<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties> recipe = BlockDryingBasin.getInstance().getRecipeRegistry().findRecipeByOutput(
                    new ItemAndFluidStackRecipeComponent(
                            new ItemStack(BlockCrystalizedMenrilBlockConfig._instance.getBlockInstance()), null));
            if (recipe != null) {
                TinkerRegistry.registerBasinCasting(recipe.getOutput().getItemStack(), null,
                        recipe.getInput().getFluidStack().getFluid(), recipe.getInput().getFluidStack().amount);
            }
    	}
    }
    
    @Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getComment() {
		return "Crystalized Menril crafting in the Casting Basin.";
	}

}
