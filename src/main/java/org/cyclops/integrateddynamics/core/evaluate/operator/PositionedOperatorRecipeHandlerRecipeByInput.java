package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeDefinition;
import org.cyclops.commoncapabilities.api.ingredient.IMixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.MixedIngredients;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeRecipe;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.util.concurrent.TimeUnit;

/**
 * An operator that gets the first recipes based on an input.
 * @author rubensworks
 */
public class PositionedOperatorRecipeHandlerRecipeByInput<T extends IValueType<V>, V extends IValue>
        extends PositionedOperatorRecipeHandler<T, V> {

    private static final Cache<Pair<Pair<DimPos, EnumFacing>, ValueObjectTypeIngredients.ValueIngredients>,
            ValueObjectTypeRecipe.ValueRecipe> CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(20, TimeUnit.SECONDS).build();

    public PositionedOperatorRecipeHandlerRecipeByInput(DimPos pos, EnumFacing side) {
        super("recipebyinput", new Function(), ValueTypes.OBJECT_RECIPE, pos, side);
    }

    public PositionedOperatorRecipeHandlerRecipeByInput() {
        this(null, null);
    }

    public static class Function extends PositionedOperatorRecipeHandlerRecipeByOutput.Function {

        protected boolean validateIngredients(IMixedIngredients actualIngredients, IMixedIngredients givenIngredients) {
            return validateIngredientsExact(actualIngredients, givenIngredients);
        }

        @Override
        protected Cache<Pair<Pair<DimPos, EnumFacing>, ValueObjectTypeIngredients.ValueIngredients>, ValueObjectTypeRecipe.ValueRecipe> getCache() {
            return CACHE;
        }

        @Override
        protected IMixedIngredients getRecipeIngredients(IRecipeDefinition recipeDefinition) {
            return MixedIngredients.fromRecipeInput(recipeDefinition);
        }
    }

}
