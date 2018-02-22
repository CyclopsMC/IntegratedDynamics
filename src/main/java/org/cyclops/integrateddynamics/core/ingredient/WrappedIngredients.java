package org.cyclops.integrateddynamics.core.ingredient;

import org.cyclops.commoncapabilities.api.ingredient.IMixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;

import java.util.List;
import java.util.Set;

/**
 * A wrapper around ingredients.
 * @author rubensworks
 */
public class WrappedIngredients implements IMixedIngredients {

    private final IMixedIngredients ingredients;

    public WrappedIngredients(IMixedIngredients ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public Set<IngredientComponent<?, ?, ?>> getComponents() {
        return ingredients.getComponents();
    }

    @Override
    public <T> List<T> getInstances(IngredientComponent<T, ?, ?> ingredientComponent) {
        return ingredients.getInstances(ingredientComponent);
    }
}
