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
import java.util.function.Predicate;

/**
 * An implementation of {@link IIngredients} by wrapping around {@link RecipeIngredients}.
 * @author rubensworks
 */
public class IngredientsRecipeIngredientsWrapper implements IIngredients {

    private final RecipeIngredients ingredients;

    public IngredientsRecipeIngredientsWrapper(RecipeIngredients ingredients) {
        this.ingredients = ingredients;
    }

    public RecipeIngredients getRecipeIngredients() {
        return ingredients;
    }

    @Override
    public Set<RecipeComponent<?, ?>> getComponents() {
        return ingredients.getComponents();
    }

    @Override
    public int getIngredients(RecipeComponent<?, ?> component) {
        return ingredients.getIngredients(component).size();
    }

    @Override
    public <V extends IValue, T, R> List<V> getList(RecipeComponent<T, R> component, int index) {
        return this.<V, T, R>getRaw(component).get(index);
    }

    protected <V extends IValue, T, R> IRecipeComponentHandler<IValueType<V>, V, T, R, RecipeComponent<T, R>>
    getHandler(RecipeComponent<T, R> component) {
        IRecipeComponentHandler<IValueType<V>, V, T, R, RecipeComponent<T, R>> handler = RecipeComponentHandlers
                .REGISTRY.getComponentHandler(component);
        if (handler == null) {
            throw new RuntimeException("No recipe component handler was found for " + component);
        }
        return handler;
    }

    @Override
    public <V extends IValue, T, R> Predicate<V> getPredicate(RecipeComponent<T, R> component, int index) {
        IRecipeComponentHandler<IValueType<V>, V, T, R, RecipeComponent<T, R>> handler = getHandler(component);
        return (instance) -> ingredients.getIngredients(component).get(index).test(handler.toInstance(instance));
    }

    @Override
    public <V extends IValue, T, R> List<List<V>> getRaw(RecipeComponent<T, R> component) {
        IRecipeComponentHandler<IValueType<V>, V, T, R, RecipeComponent<T, R>> handler = getHandler(component);
        return Lists.transform(ingredients.getIngredients(component), new Function<IRecipeIngredient<T, R>, List<V>>() {
            @Nullable
            @Override
            public List<V> apply(@Nullable IRecipeIngredient<T, R> input) {
                return Lists.transform(input.getMatchingInstances(), new Function<T, V>() {
                    @Nullable
                    @Override
                    public V apply(@Nullable T input) {
                        return handler.toValue(input);
                    }
                });
            }
        });
    }

    @Override
    public boolean equals(Object obj) {
        return IIngredients.equals(this, obj);
    }

    @Override
    public String toString() {
        return ingredients.toString();
    }
}
