package org.cyclops.integrateddynamics.core.evaluate.operator;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeDefinition;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeHandler;
import org.cyclops.commoncapabilities.api.ingredient.IMixedIngredients;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeRecipe;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * An operator that gets the recipes based on an output.
 * @author rubensworks
 */
public class PositionedOperatorRecipeHandlerRecipesByOutput<T extends IValueType<V>, V extends IValue>
        extends PositionedOperatorRecipeHandler<T, V> {

    private static final Cache<Pair<Pair<DimPos, EnumFacing>, ValueObjectTypeIngredients.ValueIngredients>,
            ValueTypeList.ValueList> CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(20, TimeUnit.SECONDS).build();

    public PositionedOperatorRecipeHandlerRecipesByOutput(DimPos pos, EnumFacing side) {
        super("recipesbyoutput", new Function(), ValueTypes.LIST, pos, side);
    }

    public PositionedOperatorRecipeHandlerRecipesByOutput() {
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
                    return getCache().get(key, () -> {
                        IMixedIngredients givenIngredients = ingredients.getRawValue().get();
                        List<ValueObjectTypeRecipe.ValueRecipe> validRecipes = Lists.newArrayList();
                        for (IRecipeDefinition recipe : recipeHandler.getRecipes()) {
                            IMixedIngredients outputIngredients = getRecipeIngredients(recipe);
                            // If one valid recipe is found, add to list
                            if (recipe.getInputComponents().size() > 0
                                    && recipe.getOutput().getComponents().size() > 0
                                    && validateIngredients(outputIngredients, givenIngredients)) {
                                validRecipes.add(ValueObjectTypeRecipe.ValueRecipe.of(recipe));
                            }
                        }
                        return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_RECIPE, validRecipes);
                    });
                } catch (ExecutionException e) {

                }
            }
            return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_RECIPE, Collections.emptyList());
        }

        protected boolean validateIngredients(IMixedIngredients actualIngredients, IMixedIngredients givenIngredients) {
            return validateIngredientsPartial(actualIngredients, givenIngredients);
        }

        protected Cache<Pair<Pair<DimPos, EnumFacing>, ValueObjectTypeIngredients.ValueIngredients>,
                ValueTypeList.ValueList> getCache() {
            return CACHE;
        }

        protected IMixedIngredients getRecipeIngredients(IRecipeDefinition recipeDefinition) {
            return recipeDefinition.getOutput();
        }
    }

}
