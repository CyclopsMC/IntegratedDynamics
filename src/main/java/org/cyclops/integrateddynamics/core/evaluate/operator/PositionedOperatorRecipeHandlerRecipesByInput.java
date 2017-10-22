package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeDefinition;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeIngredients;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.util.concurrent.TimeUnit;

/**
 * An operator that gets the recipes based on an input.
 * @author rubensworks
 */
public class PositionedOperatorRecipeHandlerRecipesByInput<T extends IValueType<V>, V extends IValue>
        extends PositionedOperatorRecipeHandler<T, V> {

    private static final Cache<Pair<Pair<DimPos, EnumFacing>, ValueObjectTypeIngredients.ValueIngredients>,
            ValueTypeList.ValueList> CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(20, TimeUnit.SECONDS).build();

    public PositionedOperatorRecipeHandlerRecipesByInput(DimPos pos, EnumFacing side) {
        super("recipesbyinput", new Function(), ValueTypes.LIST, pos, side);
    }

    public PositionedOperatorRecipeHandlerRecipesByInput() {
        this(null, null);
    }

    public static class Function extends PositionedOperatorRecipeHandlerRecipesByOutput.Function {

        protected boolean validateIngredients(RecipeIngredients actualIngredients, RecipeIngredients givenIngredients) {
            return validateIngredientsExact(actualIngredients, givenIngredients);
        }

        @Override
        protected Cache<Pair<Pair<DimPos, EnumFacing>, ValueObjectTypeIngredients.ValueIngredients>, ValueTypeList.ValueList> getCache() {
            return CACHE;
        }

        @Override
        protected RecipeIngredients getRecipeIngredients(RecipeDefinition recipeDefinition) {
            return recipeDefinition.getInput();
        }
    }

}
