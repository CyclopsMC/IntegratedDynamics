package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Iterables;
import net.minecraft.util.EnumFacing;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeDefinition;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeHandler;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;
import org.cyclops.integrateddynamics.Capabilities;

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
        IRecipeDefinition recipeDefinition = Iterables.get(getRecipeHandler().getRecipes(), index);
        return ValueObjectTypeRecipe.ValueRecipe.of(recipeDefinition);
    }
}
