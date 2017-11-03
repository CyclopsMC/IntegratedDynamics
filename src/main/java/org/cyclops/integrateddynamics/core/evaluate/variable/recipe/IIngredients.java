package org.cyclops.integrateddynamics.core.evaluate.variable.recipe;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeComponent;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeIngredients;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * For storing recipe ingredient information internally.
 * @author rubensworks
 */
public interface IIngredients {

    public static final IIngredients EMPTY = new IngredientsRecipeLists(Maps.newIdentityHashMap());

    public Set<RecipeComponent<?, ?>> getComponents();
    public int getIngredients(RecipeComponent<?, ?> component);
    public <V extends IValue, T, R> List<V> getList(RecipeComponent<T, R> component, int index);
    public <V extends IValue, T, R> Predicate<V> getPredicate(RecipeComponent<T, R> component, int index);
    public <V extends IValue, T, R> List<List<V>> getRaw(RecipeComponent<T, R> component);

    public static RecipeIngredients toRecipeIngredients(IIngredients ingredients) {
        if (ingredients instanceof IngredientsRecipeIngredientsWrapper) {
            return ((IngredientsRecipeIngredientsWrapper) ingredients).getRecipeIngredients();
        } else {
            return new RecipeIngredientsIngredientsWrapper(ingredients);
        }
    }

    public static IIngredients orEmpty(Optional<IIngredients> optionalIngredients) {
        return optionalIngredients.or(EMPTY);
    }

    public static boolean equals(IIngredients self, Object obj) {
        if (self == obj) {
            return true;
        }

        if (obj instanceof IIngredients) {
            IIngredients that = (IIngredients) obj;
            if (self.getComponents().equals(that.getComponents())) {
                return false;
            }
            for (RecipeComponent component : self.getComponents()) {
                if (!self.getRaw(component).equals(that.getRaw(component))) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

}
