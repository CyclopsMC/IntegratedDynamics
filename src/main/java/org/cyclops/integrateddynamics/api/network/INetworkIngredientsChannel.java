package org.cyclops.integrateddynamics.api.network;

import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.integrateddynamics.api.part.PartPos;

import javax.annotation.Nonnull;

/**
 * @author rubensworks
 */
public interface INetworkIngredientsChannel<T, M> extends IIngredientComponentStorage<T, M> {

    public Iterable<PartPos> findNonFullPositions();
    public Iterable<PartPos> findAllPositions();
    public Iterable<PartPos> findNonEmptyPositions();
    public Iterable<PartPos> findMatchingPositions(@Nonnull T prototype, M matchFlags);

}
