package org.cyclops.integrateddynamics.api.network;

import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.integrateddynamics.api.part.PartPos;

import javax.annotation.Nonnull;

/**
 * @author rubensworks
 */
public interface INetworkIngredientsChannel<T, M> extends IIngredientComponentStorage<T, M> {

    /**
     * Insert an ingredient of which a part was already accounted for before it reached this channel.
     *
     * This is needed when an ingredient is first offered to an insert pre-consumer outside of this channel,
     * so that the pre-consumers of this channel can not account for that same part a second time.
     *
     * @param ingredient The ingredient to insert.
     * @param unaccounted The part of the ingredient that no insert pre-consumer has accounted for yet.
     * @param simulate If the insertion should be simulated.
     * @return The ingredient that could not be inserted.
     */
    public default T insert(@Nonnull T ingredient, @Nonnull T unaccounted, boolean simulate) {
        return insert(ingredient, simulate);
    }

    public Iterable<PartPos> findNonFullPositions();
    public Iterable<PartPos> findAllPositions();
    public Iterable<PartPos> findNonEmptyPositions();
    public Iterable<PartPos> findMatchingPositions(@Nonnull T prototype, M matchFlags);

}
