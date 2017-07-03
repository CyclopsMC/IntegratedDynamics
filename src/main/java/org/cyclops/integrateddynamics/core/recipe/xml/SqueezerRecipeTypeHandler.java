package org.cyclops.integrateddynamics.core.recipe.xml;

import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.init.RecipeHandler;
import org.cyclops.cyclopscore.recipe.custom.api.IRecipe;
import org.cyclops.cyclopscore.recipe.custom.component.DummyPropertiesComponent;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientAndFluidStackRecipeComponent;
import org.cyclops.cyclopscore.recipe.custom.component.IngredientRecipeComponent;
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
public class SqueezerRecipeTypeHandler extends SuperRecipeTypeHandler<IngredientRecipeComponent, IngredientAndFluidStackRecipeComponent, DummyPropertiesComponent> {

    @Override
    public String getCategoryId() {
        return Reference.MOD_ID + ":squeezer_recipe";
    }

	@Override
	protected IRecipe<IngredientRecipeComponent, IngredientAndFluidStackRecipeComponent, DummyPropertiesComponent> handleRecipe(RecipeHandler recipeHandler, Element input, Element output, Element properties)
			throws XmlRecipeLoader.XmlRecipeException {
        Ingredient inputItem = Ingredient.EMPTY;
        Ingredient outputItem = Ingredient.EMPTY;
        FluidStack outputFluid = null;

        if(input.getElementsByTagName("item").getLength() > 0) {
            inputItem = getIngredient(recipeHandler, input.getElementsByTagName("item").item(0));
        }

        if(output.getElementsByTagName("item").getLength() > 0) {
            outputItem = getIngredient(recipeHandler, output.getElementsByTagName("item").item(0));
        }
        if(output.getElementsByTagName("fluid").getLength() > 0) {
            outputFluid = getFluid(recipeHandler, output.getElementsByTagName("fluid").item(0));
        }

        if(inputItem == null && outputFluid == null) {
            throw new XmlRecipeLoader.XmlRecipeException("Squeezer recipes must have an output item or fluid.");
        }

        IngredientRecipeComponent inputRecipeComponent = new IngredientRecipeComponent(inputItem);

        IngredientAndFluidStackRecipeComponent outputRecipeComponent = new IngredientAndFluidStackRecipeComponent(outputItem, outputFluid);

		return BlockSqueezer.getInstance().getRecipeRegistry().registerRecipe(
                inputRecipeComponent,
                outputRecipeComponent,
                new DummyPropertiesComponent()
        );
	}

}
