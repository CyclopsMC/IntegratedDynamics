package org.cyclops.integrateddynamics.api.ingredient.capability;

import java.util.Optional;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;

/**
 * A capability that retrieves the {@link IPositionedAddonsNetworkIngredients}
 * of an {@link org.cyclops.commoncapabilities.api.ingredient.IngredientComponent} in a network.
 *
 * @param <T> The instance type.
 * @param <M> The matching condition parameter, may be Void. Instances MUST properly implement the equals method.
 * @author rubensworks
 */
public interface IPositionedAddonsNetworkIngredientsHandler<T, M> {

    /**
     * Get the ingredient network in the given network,
     * @param network The network.
     * @return The optional ingredient component network.
     */
    public Optional<IPositionedAddonsNetworkIngredients<T, M>> getStorage(INetwork network);

}
