package org.cyclops.integrateddynamics.core.evaluate.variable;

import com.google.common.collect.Iterables;
import net.minecraft.core.Direction;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeHandler;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;

import java.util.Optional;

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

    protected Optional<IRecipeHandler> getRecipeHandler() {
        return Optional.ofNullable(getPos().getLevel(true).getCapability(org.cyclops.commoncapabilities.api.capability.Capabilities.RecipeHandler.BLOCK, getPos().getBlockPos(), getSide()));
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
