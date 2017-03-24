package org.cyclops.integrateddynamics.core.recipe.xml;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.init.RecipeHandler;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.ItemAndFluidStackRecipeComponent;
import org.cyclops.cyclopscore.recipe.xml.SuperRecipeTypeHandler;
import org.cyclops.cyclopscore.recipe.xml.XmlRecipeLoader;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.block.BlockDryingBasin;
import org.w3c.dom.Element;

/**
 * Handler for drying basin recipes.
 * @author rubensworks
 *
 */
public class DryingBasinRecipeTypeHandler extends SuperRecipeTypeHandler<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties> {

    @Override
    public String getCategoryId() {
        return Reference.MOD_ID + ":dryingBasinRecipe";
    }

	@Override
	protected IRecipe<ItemAndFluidStackRecipeComponent, ItemAndFluidStackRecipeComponent, DurationRecipeProperties> handleRecipe(RecipeHandler recipeHandler, Element input, Element output, Element properties)
			throws XmlRecipeLoader.XmlRecipeException {
        Object inputItem = null;
        ItemStack outputItem = null;
        FluidStack inputFluid = null;
        FluidStack outputFluid = null;

        if(input.getElementsByTagName("item").getLength() > 0) {
            inputItem = getItem(recipeHandler, input.getElementsByTagName("item").item(0));
        }
        if(input.getElementsByTagName("fluid").getLength() > 0) {
            inputFluid = getFluid(recipeHandler, input.getElementsByTagName("fluid").item(0));
        }

        if(output.getElementsByTagName("item").getLength() > 0) {
            outputItem = (ItemStack) getItem(recipeHandler, output.getElementsByTagName("item").item(0));
        }
        if(output.getElementsByTagName("fluid").getLength() > 0) {
            outputFluid = getFluid(recipeHandler, output.getElementsByTagName("fluid").item(0));
        }

        if(inputFluid != null && outputFluid != null) {
            throw new XmlRecipeLoader.XmlRecipeException(String.format("Can't have an input and output fluid: %s and %s", inputFluid.getLocalizedName(), outputFluid.getLocalizedName()));
        }

		int duration = Integer.parseInt(properties.getElementsByTagName("duration").item(0).getTextContent());

        ItemAndFluidStackRecipeComponent inputRecipeComponent;
        if(inputItem == null || inputItem instanceof ItemStack) {
            inputRecipeComponent = new ItemAndFluidStackRecipeComponent((ItemStack) inputItem, inputFluid);
        } else {
            inputRecipeComponent = new ItemAndFluidStackRecipeComponent((String) inputItem, inputFluid);
        }

        ItemAndFluidStackRecipeComponent outputRecipeComponent = new ItemAndFluidStackRecipeComponent(outputItem, outputFluid);
		return BlockDryingBasin.getInstance().getRecipeRegistry().registerRecipe(
                inputRecipeComponent,
                outputRecipeComponent,
                new DurationRecipeProperties(duration)
        );
	}
}
