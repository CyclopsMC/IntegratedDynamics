package org.cyclops.integrateddynamics.core.recipe.xml;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.init.RecipeHandler;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.component.DummyPropertiesComponent;
import org.cyclops.cyclopscore.recipe.custom.component.ItemAndFluidStackRecipeComponent;
import org.cyclops.cyclopscore.recipe.custom.component.ItemStackRecipeComponent;
import org.cyclops.cyclopscore.recipe.custom.component.OreDictItemStackRecipeComponent;
import org.cyclops.cyclopscore.recipe.xml.SuperRecipeTypeHandler;
import org.cyclops.cyclopscore.recipe.xml.XmlRecipeLoader;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.block.BlockSqueezer;
import org.w3c.dom.Element;

/**
 * Handler for squeezer recipes.
 * @author rubensworks
 *
 */
public class SqueezerRecipeTypeHandler extends SuperRecipeTypeHandler<ItemStackRecipeComponent, ItemAndFluidStackRecipeComponent, DummyPropertiesComponent> {

    @Override
    public String getCategoryId() {
        return Reference.MOD_ID + ":squeezerRecipe";
    }

	@Override
	protected IRecipe<ItemStackRecipeComponent, ItemAndFluidStackRecipeComponent, DummyPropertiesComponent> handleRecipe(RecipeHandler recipeHandler, Element input, Element output, Element properties)
			throws XmlRecipeLoader.XmlRecipeException {
        Object inputItem = null;
        Object outputItem = null;
        FluidStack outputFluid = null;

        if(input.getElementsByTagName("item").getLength() > 0) {
            inputItem = getItem(recipeHandler, input.getElementsByTagName("item").item(0));
        }

        if(output.getElementsByTagName("item").getLength() > 0) {
            outputItem = getItem(recipeHandler, output.getElementsByTagName("item").item(0));
        }
        if(output.getElementsByTagName("fluid").getLength() > 0) {
            outputFluid = getFluid(recipeHandler, output.getElementsByTagName("fluid").item(0));
        }

        if(inputItem == null && outputFluid == null) {
            throw new XmlRecipeLoader.XmlRecipeException("Squeezer recipes must have an output item or fluid.");
        }

        ItemStackRecipeComponent inputRecipeComponent;
        if(inputItem instanceof ItemStack) {
            inputRecipeComponent = new ItemStackRecipeComponent((ItemStack) inputItem);
        } else {
            inputRecipeComponent = new OreDictItemStackRecipeComponent((String) inputItem);
        }

        ItemAndFluidStackRecipeComponent outputRecipeComponent;
        if(outputItem == null || outputItem instanceof ItemStack) {
            outputRecipeComponent = new ItemAndFluidStackRecipeComponent((ItemStack) outputItem, outputFluid);
        } else {
            outputRecipeComponent = new ItemAndFluidStackRecipeComponent((String) outputItem, outputFluid);
        }


		return BlockSqueezer.getInstance().getRecipeRegistry().registerRecipe(
                inputRecipeComponent,
                outputRecipeComponent,
                new DummyPropertiesComponent()
        );
	}

}
