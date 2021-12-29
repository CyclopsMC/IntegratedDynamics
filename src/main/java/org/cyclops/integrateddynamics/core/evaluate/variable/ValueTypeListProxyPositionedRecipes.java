package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Iterables;
import net.minecraft.core.Direction;
import net.minecraftforge.common.util.LazyOptional;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeHandler;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.modcompat.commoncapabilities.BlockCapabilitiesHelpers;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;
import org.cyclops.integrateddynamics.Capabilities;

/**
 * A list proxy for the recipes of a recipe handler at a certain position.
 */
public class ValueTypeListProxyPositionedRecipes extends ValueTypeListProxyPositioned<ValueObjectTypeRecipe, ValueObjectTypeRecipe.ValueRecipe> implements INBTProvider {

    public ValueTypeListProxyPositionedRecipes(DimPos pos, Direction side) {
        super(ValueTypeListProxyFactories.POSITIONED_RECIPES.getName(), ValueTypes.OBJECT_RECIPE, pos, side);
    }

    public ValueTypeListProxyPositionedRecipes() {
        this(null, null);
    }

    protected LazyOptional<IRecipeHandler> getRecipeHandler() {
        return BlockCapabilitiesHelpers.getTileOrBlockCapability(getPos(), getSide(), Capabilities.RECIPE_HANDLER);
    }

    @Override
    public int getLength() {
        return getRecipeHandler()
                .map(recipeHandler -> recipeHandler.getRecipes().size())
                .orElse(0);
    }

    @Override
    public ValueObjectTypeRecipe.ValueRecipe get(int index) {
        return ValueObjectTypeRecipe.ValueRecipe.of(getRecipeHandler()
                .map(recipeHandler -> Iterables.get(recipeHandler.getRecipes(), index))
                .orElse(null));
    }
}
