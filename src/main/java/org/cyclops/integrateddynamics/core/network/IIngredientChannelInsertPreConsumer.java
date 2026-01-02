package org.cyclops.integrateddynamics.core.network;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nonnull;

/**
 * Allows (partially) consuming an ingredient before it is inserted into the network.
 * @author rubensworks
 */
public interface IIngredientChannelInsertPreConsumer<T> {

    /**
     * Called before an ingredient is inserted into the network.
     * If nothing needs to be consumed, the same instance can be returned.
     * @param channel The network channel.
     * @param ingredient The ingredient instance.
     * @param transaction The current transaction.
     * @return The remaining ingredient instance.
     */
    public T insert(int channel, @Nonnull T ingredient, TransactionContext transaction);

}
