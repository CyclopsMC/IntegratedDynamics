package org.cyclops.integrateddynamics.core.recipe.xml;

import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.init.RecipeHandler;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.component.DurationRecipeProperties;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientAndFluidStackRecipeComponent;
import org.cyclops.cyclopscore.recipe.xml.SuperRecipeTypeHandler;
import org.cyclops.cyclopscore.recipe.xml.XmlRecipeLoader;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.block.BlockMechanicalDryingBasin;
import org.w3c.dom.Element;

/**
 * Handler for mechanical drying basin recipes.
 * @author rubensworks
 *
 */
public class MechanicalDryingBasinRecipeTypeHandler extends SuperRecipeTypeHandler<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> {

    @Override
    public String getCategoryId() {
        return Reference.MOD_ID + ":mechanical_drying_basin_recipe";
    }

	@Override
	protected IRecipe<IngredientAndFluidStackRecipeComponent, IngredientAndFluidStackRecipeComponent, DurationRecipeProperties> handleRecipe(RecipeHandler recipeHandler, Element input, Element output, Element properties)
			throws XmlRecipeLoader.XmlRecipeException {
        Ingredient inputItem = Ingredient.EMPTY;
        Ingredient outputItem = Ingredient.EMPTY;
        FluidStack inputFluid = null;
        FluidStack outputFluid = null;

        if(input.getElementsByTagName("item").getLength() > 0) {
            inputItem = getIngredient(recipeHandler, input.getElementsByTagName("item").item(0));
        }
        if(input.getElementsByTagName("fluid").getLength() > 0) {
            inputFluid = getFluid(recipeHandler, input.getElementsByTagName("fluid").item(0));
        }

        if(output.getElementsByTagName("item").getLength() > 0) {
            outputItem = getIngredient(recipeHandler, output.getElementsByTagName("item").item(0));
        }
        if(output.getElementsByTagName("fluid").getLength() > 0) {
            outputFluid = getFluid(recipeHandler, output.getElementsByTagName("fluid").item(0));
        }

        if(inputFluid != null && outputFluid != null) {
            throw new XmlRecipeLoader.XmlRecipeException(String.format("Can't have an input and output fluid: %s and %s", inputFluid.getLocalizedName(), outputFluid.getLocalizedName()));
        }

		int duration = Integer.parseInt(properties.getElementsByTagName("duration").item(0).getTextContent());

        IngredientAndFluidStackRecipeComponent inputRecipeComponent = new IngredientAndFluidStackRecipeComponent(inputItem, inputFluid);

        IngredientAndFluidStackRecipeComponent outputRecipeComponent = new IngredientAndFluidStackRecipeComponent(outputItem, outputFluid);
		return BlockMechanicalDryingBasin.getInstance().getRecipeRegistry().registerRecipe(
                inputRecipeComponent,
                outputRecipeComponent,
                new DurationRecipeProperties(duration)
        );
	}
}
