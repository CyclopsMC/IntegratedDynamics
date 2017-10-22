package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.util.EnumFacing;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeHandler;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeDefinition;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.IngredientsRecipeIngredientsWrapper;

/**
 * A list proxy for the recipes of a recipe handler at a certain position.
 */
public class ValueTypeListProxyPositionedRecipes extends ValueTypeListProxyPositioned<ValueObjectTypeRecipe, ValueObjectTypeRecipe.ValueRecipe> implements INBTProvider {

    public ValueTypeListProxyPositionedRecipes(DimPos pos, EnumFacing side) {
        super(ValueTypeListProxyFactories.POSITIONED_RECIPES.getName(), ValueTypes.OBJECT_RECIPE, pos, side);
    }

    protected IRecipeHandler getRecipeHandler() {
        return Helpers.getTileOrBlockCapability(getPos().getWorld(), getPos().getBlockPos(), getSide(),
                Capabilities.RECIPE_HANDLER);
    }

    @Override
    public int getLength() {
        IRecipeHandler recipeHandler = getRecipeHandler();
        if(recipeHandler == null) {
            return 0;
        }
        return recipeHandler.getRecipes().size();
    }

    @Override
    public ValueObjectTypeRecipe.ValueRecipe get(int index) {
        RecipeDefinition recipeDefinition = getRecipeHandler().getRecipes().get(index);
        return ValueObjectTypeRecipe.ValueRecipe.of(new ValueObjectTypeRecipe.Recipe(
                ValueObjectTypeIngredients.ValueIngredients.of(new IngredientsRecipeIngredientsWrapper(recipeDefinition.getInput())),
                ValueObjectTypeIngredients.ValueIngredients.of(new IngredientsRecipeIngredientsWrapper(recipeDefinition.getOutput())
                )));
    }
}
