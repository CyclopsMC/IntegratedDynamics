package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeHandler;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeIngredients;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.IIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.IngredientsRecipeIngredientsWrapper;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * An operator that gets the output of a recipe based on an input.
 * @author rubensworks
 */
public class PositionedOperatorRecipeHandlerOutput<T extends IValueType<V>, V extends IValue> extends PositionedOperatorRecipeHandler<T, V> {

    private static final Cache<Pair<Pair<DimPos, EnumFacing>, ValueObjectTypeIngredients.ValueIngredients>,
            ValueObjectTypeIngredients.ValueIngredients> CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(20, TimeUnit.SECONDS).build();

    public PositionedOperatorRecipeHandlerOutput(DimPos pos, EnumFacing side) {
        super("recipeoutputbyinput", new Function(), pos, side);
    }

    public PositionedOperatorRecipeHandlerOutput() {
        this(null, null);
    }

    public static class Function extends PositionedOperatorRecipeHandler.Function {

        @Override
        public IValue evaluate(SafeVariablesGetter variables) throws EvaluationException {
            ValueObjectTypeIngredients.ValueIngredients ingredients = variables.getValue(0);
            IRecipeHandler recipeHandler = this.getOperator().getRecipeHandler();
            if (recipeHandler != null && ingredients.getRawValue().isPresent()) {
                Pair<Pair<DimPos, EnumFacing>, ValueObjectTypeIngredients.ValueIngredients> key =
                        Pair.of(Pair.of(this.getOperator().getPos(), this.getOperator().getSide()), ingredients);
                try {
                    return CACHE.get(key, () -> {
                        RecipeIngredients output = recipeHandler.simulate(
                                IIngredients.toRecipeIngredients(ingredients.getRawValue().get()));
                        return ValueObjectTypeIngredients.ValueIngredients.of(
                                new IngredientsRecipeIngredientsWrapper(output));
                    });
                } catch (ExecutionException e) {

                }
            }
            return ValueObjectTypeIngredients.ValueIngredients.of(null);
        }
    }

}
