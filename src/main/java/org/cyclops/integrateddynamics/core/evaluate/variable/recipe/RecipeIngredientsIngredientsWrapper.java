package org.cyclops.integrateddynamics.core.evaluate.variable.recipe;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeIngredient;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeComponent;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeIngredients;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.recipe.IRecipeComponentHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * An implementation of {@link RecipeIngredients} by wrapping around {@link IIngredients}.
 * @author rubensworks
 */
public class RecipeIngredientsIngredientsWrapper extends RecipeIngredients {

    private final IIngredients ingredients;

    public RecipeIngredientsIngredientsWrapper(IIngredients ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public int getIngredientsSize() {
        return this.ingredients.getComponents().stream().mapToInt(this.ingredients::getIngredients).sum();
    }

    @Override
    public Set<RecipeComponent<?, ?>> getComponents() {
        return this.ingredients.getComponents();
    }

    @Override
    public <T, R> List<IRecipeIngredient<T, R>> getIngredients(RecipeComponent<T, R> component) {
        return Lists.transform(ingredients.getRaw(component), new Function<List<IValue>, IRecipeIngredient<T, R>>() {
            @Nullable
            @Override
            public IRecipeIngredient<T, R> apply(@Nullable List<IValue> input) {
                return new RecipeIngredientValue<>(input, component);
            }
        });
    }

    public IIngredients getIngredients() {
        return ingredients;
    }

    // toString and equals should not be necessary, as this wrapper is only for internal delegation usage,
    // not for storing in IValues.

    public static class RecipeIngredientValue<VT extends IValueType<V>, V extends IValue,
            T, R, C extends RecipeComponent<T, R>> implements IRecipeIngredient<T, R> {

        private final List<V> values;
        private final C recipeComponent;
        private final IRecipeComponentHandler<VT, V, T, R, C> recipeComponentHandler;

        public RecipeIngredientValue(List<V> values, C component) {
            this.values = values;
            this.recipeComponent = component;
            this.recipeComponentHandler = RecipeComponentHandlers.REGISTRY.getComponentHandler(component);
        }

        @Override
        public RecipeComponent<T, R> getComponent() {
            return this.recipeComponent;
        }

        @Override
        public List<T> getMatchingInstances() {
            return Lists.transform(this.values, new Function<V, T>() {
                @Nullable
                @Override
                public T apply(@Nullable V input) {
                    return recipeComponentHandler.toInstance(input);
                }
            });
        }

        @Override
        public boolean test(T t) {
            return this.values.contains(recipeComponentHandler.toValue(t));
        }
    }
}
