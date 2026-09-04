package org.cyclops.integrateddynamics.core.network;

import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;

import javax.annotation.Nonnull;
import java.util.Collection;

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
     * @param simulate Simulation mode or not.
     * @return The remaining ingredient instance.
     * @deprecated Implement {@link #insert(int, Object, Object, boolean)} instead,
     *             which also tracks the part of the instance that was not accounted for yet.
     */
    @Deprecated // TODO: rm in next major
    public default T insert(int channel, @Nonnull T ingredient, boolean simulate) {
        return ingredient;
    }

    /**
     * Called before an ingredient is inserted into the network.
     *
     * All pre-consumers of a network observe the same insertion, one after the other.
     * A pre-consumer that attributes (part of) an insertion to itself without consuming it,
     * such as one that waits for an ingredient to be produced,
     * must therefore only look at the unaccounted part of the instance,
     * so that one instance is never attributed to multiple pre-consumers.
     *
     * The remaining instance is what is left to be inserted into the network,
     * which is only reduced by the part that is effectively consumed here.
     * The unaccounted instance is what no pre-consumer has attributed to itself yet,
     * which is reduced both by what is consumed and by what is only attributed.
     *
     * @param channel The network channel.
     * @param ingredient The remaining ingredient instance.
     * @param unaccounted The part of the ingredient instance that was not accounted for yet.
     *                    This is never larger than the remaining instance.
     * @param simulate Simulation mode or not.
     * @return The remaining and unaccounted ingredient instances.
     */
    public default Result<T> insert(int channel, @Nonnull T ingredient, @Nonnull T unaccounted, boolean simulate) {
        return new Result<>(insert(channel, ingredient, simulate), unaccounted);
    }

    /**
     * Run the given ingredient instance through all the given pre-consumers.
     * @param preConsumers The pre-consumers of the network channel.
     * @param matcher The matcher of the ingredient component.
     * @param channel The network channel.
     * @param ingredient The ingredient instance that is being inserted.
     * @param unaccounted The part of the ingredient instance that was not accounted for yet,
     *                    which is the whole instance unless a pre-consumer was already applied to it.
     * @param simulate Simulation mode or not.
     * @return The remaining ingredient instance that still has to be inserted into the network.
     */
    public static <T, M> T applyAll(Collection<IIngredientChannelInsertPreConsumer<T>> preConsumers,
                                    IIngredientMatcher<T, M> matcher, int channel, @Nonnull T ingredient,
                                    @Nonnull T unaccounted, boolean simulate) {
        for (IIngredientChannelInsertPreConsumer<T> preConsumer : preConsumers) {
            Result<T> result = preConsumer.insert(channel, ingredient, unaccounted, simulate);
            ingredient = result.remaining();
            unaccounted = result.unaccounted();

            // Pre-consumers on the deprecated api consume without accounting, so restore the invariant
            long remainingQuantity = matcher.getQuantity(ingredient);
            if (matcher.getQuantity(unaccounted) > remainingQuantity) {
                unaccounted = matcher.withQuantity(unaccounted, remainingQuantity);
            }
        }
        return ingredient;
    }

    /**
     * The outcome of a pre-consumer insertion.
     * @param remaining The ingredient instance that still has to be inserted into the network.
     * @param unaccounted The part of the ingredient instance that was not accounted for yet.
     */
    public static record Result<T>(T remaining, T unaccounted) {
    }

}
