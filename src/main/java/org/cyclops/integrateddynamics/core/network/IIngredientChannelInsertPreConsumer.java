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
     *             which also tracks the part of the insertion that is still unclaimed.
     */
    @Deprecated // TODO: rm in next major
    public default T insert(int channel, @Nonnull T ingredient, boolean simulate) {
        return ingredient;
    }

    /**
     * Called before an ingredient is inserted into the network.
     *
     * All pre-consumers of a network observe the same insertion, one after the other,
     * each one receiving what the previous one left.
     * A pre-consumer can do two independent things with it, see {@link Result}:
     * it can take part of it away, and it can claim part of it.
     * Only the unclaimed part may be claimed,
     * so that one insertion is never claimed by multiple pre-consumers.
     *
     * @param channel The network channel.
     * @param ingredient The part of the insertion that still has to be inserted into the network.
     * @param unclaimed The part of the insertion that no pre-consumer has claimed yet.
     *                  This is never larger than the given ingredient.
     * @param simulate Simulation mode or not.
     * @return What is left to insert into the network, and what of it is left unclaimed.
     */
    public default Result<T> insert(int channel, @Nonnull T ingredient, @Nonnull T unclaimed, boolean simulate) {
        return new Result<>(insert(channel, ingredient, simulate), unclaimed);
    }

    /**
     * Run the given ingredient instance through all the given pre-consumers.
     * @param preConsumers The pre-consumers of the network channel.
     * @param matcher The matcher of the ingredient component.
     * @param channel The network channel.
     * @param ingredient The ingredient instance that is being inserted.
     * @param unclaimed The part of the insertion that no pre-consumer has claimed yet,
     *                  which is the whole instance unless a pre-consumer was already applied to it.
     * @param simulate Simulation mode or not.
     * @return The remaining ingredient instance that still has to be inserted into the network.
     */
    public static <T, M> T applyAll(Collection<IIngredientChannelInsertPreConsumer<T>> preConsumers,
                                    IIngredientMatcher<T, M> matcher, int channel, @Nonnull T ingredient,
                                    @Nonnull T unclaimed, boolean simulate) {
        for (IIngredientChannelInsertPreConsumer<T> preConsumer : preConsumers) {
            Result<T> result = preConsumer.insert(channel, ingredient, unclaimed, simulate);
            ingredient = result.remaining();
            unclaimed = result.unclaimed();

            // Pre-consumers on the deprecated api take away without claiming, so restore the invariant
            long remainingQuantity = matcher.getQuantity(ingredient);
            if (matcher.getQuantity(unclaimed) > remainingQuantity) {
                unclaimed = matcher.withQuantity(unclaimed, remainingQuantity);
            }
        }
        return ingredient;
    }

    /**
     * The outcome of a pre-consumer insertion.
     *
     * These two are tracked separately because a pre-consumer can do two independent things
     * with the insertion it observes:
     *
     * It can <i>take part of it away</i>, so that that part is no longer inserted into the network.
     * A crafting interface does this with the produced ingredients that a dependent crafting job
     * still needs as an input, which it moves into that job's buffer.
     *
     * It can also <i>claim part of it</i>, without taking it away:
     * it recognizes that part as the ingredient it was waiting for, but the ingredient itself
     * still has to reach the network storage. A crafting interface does this with the outputs of
     * its own crafting jobs: seeing the output is what completes the job, but the output is
     * exactly what the network asked for, so it must still be stored.
     *
     * Taking away reduces both, claiming only reduces the unclaimed part.
     * Claiming has to be tracked separately for this reason:
     * an insertion that is only claimed is passed on to the next pre-consumer unchanged,
     * so without it, every pre-consumer would claim that same insertion for itself.
     *
     * @param remaining What still has to be inserted into the network.
     *                  Only what a pre-consumer took away is missing from this.
     * @param unclaimed What no pre-consumer has claimed yet, and a next one may still claim.
     *                  Both what was taken away and what was only claimed is missing from this,
     *                  so this is never larger than the remaining part.
     */
    public static record Result<T>(T remaining, T unclaimed) {
    }

}
