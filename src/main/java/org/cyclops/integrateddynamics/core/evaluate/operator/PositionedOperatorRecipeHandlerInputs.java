package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeHandler;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeDefinition;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeIngredients;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.IIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.IngredientsRecipeIngredientsWrapper;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * An operator that gets the input of a recipe based on an output.
 * @author rubensworks
 */
public class PositionedOperatorRecipeHandlerInputs<T extends IValueType<V>, V extends IValue> extends PositionedOperatorRecipeHandler<T, V> {

    private static final Cache<Pair<Pair<DimPos, EnumFacing>, ValueObjectTypeIngredients.ValueIngredients>,
            ValueTypeList.ValueList> CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(20, TimeUnit.SECONDS).build();

    public PositionedOperatorRecipeHandlerInputs(DimPos pos, EnumFacing side) {
        super("recipeinputsbyoutput", new Function(), ValueTypes.LIST, pos, side);
    }

    public PositionedOperatorRecipeHandlerInputs() {
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
                        RecipeIngredients givenIngredients = IIngredients.toRecipeIngredients(ingredients.getRawValue().get());
                        List<ValueObjectTypeIngredients.ValueIngredients> validIngredients = Lists.newArrayList();
                        for (RecipeDefinition recipe : recipeHandler.getRecipes()) {
                            RecipeIngredients outputIngredients = recipe.getOutput();
                            // If one valid recipe is found, add to list
                            if (validateIngredientsPartial(outputIngredients, givenIngredients)) {
                                validIngredients.add(ValueObjectTypeIngredients.ValueIngredients.of(
                                        new IngredientsRecipeIngredientsWrapper(recipe.getInput())));
                            }
                        }
                        return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_INGREDIENTS, validIngredients);
                    });
                } catch (ExecutionException e) {

                }
            }
            return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_INGREDIENTS, Collections.emptyList());
        }
    }

}
