package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.Lists;
import net.minecraft.network.chat.MutableComponent;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.junit.Test;



import static org.junit.Assert.assertEquals;

/**
 * Tests for how insert pre-consumers are chained on an ingredient channel.
 *
 * Instances are plain longs here, where the value is the quantity.
 *
 * @author rubensworks
 */
public class TestIngredientChannelInsertPreConsumer {

    private static final IIngredientMatcher<Long, Boolean> MATCHER = new LongMatcher();

    @Test
    public void testClaimingPreConsumersDoNotShareTheSameInstance() {
        ClaimingPreConsumer first = new ClaimingPreConsumer(1);
        ClaimingPreConsumer second = new ClaimingPreConsumer(1);

        long remaining = IIngredientChannelInsertPreConsumer.applyAll(
                Lists.newArrayList(first, second), MATCHER, 0, 1L, 1L, false);

        assertEquals("only the first pre-consumer claims the instance", 1L, first.claimed);
        assertEquals("the second pre-consumer sees nothing left to claim", 0L, second.claimed);
        assertEquals("the instance is still inserted into the network", 1L, remaining);
    }

    @Test
    public void testClaimingPreConsumersSplitTheSameInstance() {
        ClaimingPreConsumer first = new ClaimingPreConsumer(2);
        ClaimingPreConsumer second = new ClaimingPreConsumer(5);

        long remaining = IIngredientChannelInsertPreConsumer.applyAll(
                Lists.newArrayList(first, second), MATCHER, 0, 3L, 3L, false);

        assertEquals(2L, first.claimed);
        assertEquals(1L, second.claimed);
        assertEquals(3L, remaining);
    }

    @Test
    public void testQuantityClaimedBeforeTheChannelIsNotClaimedAgain() {
        ClaimingPreConsumer preConsumer = new ClaimingPreConsumer(5);

        long remaining = IIngredientChannelInsertPreConsumer.applyAll(
                Lists.newArrayList(preConsumer), MATCHER, 0, 3L, 1L, false);

        assertEquals("only the part that was not claimed yet can be claimed", 1L, preConsumer.claimed);
        assertEquals(3L, remaining);
    }

    @Test
    public void testTakenQuantityCanNotBeClaimedAgain() {
        ConsumingPreConsumer first = new ConsumingPreConsumer(2);
        ClaimingPreConsumer second = new ClaimingPreConsumer(5);

        long remaining = IIngredientChannelInsertPreConsumer.applyAll(
                Lists.newArrayList(first, second), MATCHER, 0, 3L, 3L, false);

        assertEquals("what is taken away can not be claimed", 1L, second.claimed);
        assertEquals(1L, remaining);
    }

    @Test
    public void testDeprecatedPreConsumersCanNotBeOverClaimed() {
        DeprecatedConsumingPreConsumer first = new DeprecatedConsumingPreConsumer(2);
        ClaimingPreConsumer second = new ClaimingPreConsumer(5);

        long remaining = IIngredientChannelInsertPreConsumer.applyAll(
                Lists.newArrayList(first, second), MATCHER, 0, 3L, 3L, false);

        assertEquals("the unclaimed quantity never exceeds the remaining quantity", 1L, second.claimed);
        assertEquals(1L, remaining);
    }

    /**
     * Claims the instance for itself without taking it away, like a pre-consumer that awaits an output.
     */
    private static class ClaimingPreConsumer implements IIngredientChannelInsertPreConsumer<Long> {

        private final long capacity;
        private long claimed;

        public ClaimingPreConsumer(long capacity) {
            this.capacity = capacity;
        }

        @Override
        public Result<Long> insert(int channel, Long ingredient, Long unclaimed, boolean simulate) {
            long claiming = Math.min(this.capacity - this.claimed, unclaimed);
            this.claimed += claiming;
            return new Result<>(ingredient, unclaimed - claiming);
        }
    }

    /**
     * Takes part of the instance away, so that it is not inserted into the network.
     */
    private static class ConsumingPreConsumer implements IIngredientChannelInsertPreConsumer<Long> {

        private final long capacity;

        public ConsumingPreConsumer(long capacity) {
            this.capacity = capacity;
        }

        @Override
        public Result<Long> insert(int channel, Long ingredient, Long unclaimed, boolean simulate) {
            long taken = Math.min(this.capacity, unclaimed);
            return new Result<>(ingredient - taken, unclaimed - taken);
        }
    }

    /**
     * Takes part of the instance away through the deprecated api, which is unaware of claiming.
     */
    private static class DeprecatedConsumingPreConsumer implements IIngredientChannelInsertPreConsumer<Long> {

        private final long capacity;

        public DeprecatedConsumingPreConsumer(long capacity) {
            this.capacity = capacity;
        }

        @Override
        public Long insert(int channel, Long ingredient, boolean simulate) {
            return ingredient - Math.min(this.capacity, ingredient);
        }
    }

    private static class LongMatcher implements IIngredientMatcher<Long, Boolean> {

        @Override
        public boolean isInstance(Object object) {
            return object instanceof Long;
        }

        @Override
        public Boolean getAnyMatchCondition() {
            return false;
        }

        @Override
        public Boolean getExactMatchCondition() {
            return true;
        }

        @Override
        public Boolean getExactMatchNoQuantityCondition() {
            return false;
        }

        @Override
        public Boolean withCondition(Boolean matchCondition, Boolean with) {
            return matchCondition || with;
        }

        @Override
        public Boolean withoutCondition(Boolean matchCondition, Boolean without) {
            return matchCondition == without ? false : matchCondition;
        }

        @Override
        public boolean hasCondition(Boolean matchCondition, Boolean searchCondition) {
            return matchCondition == searchCondition;
        }

        @Override
        public boolean matches(Long a, Long b, Boolean matchCondition) {
            return !matchCondition || a.longValue() == b.longValue();
        }

        @Override
        public Long getEmptyInstance() {
            return 0L;
        }

        @Override
        public int hash(Long instance) {
            return instance.hashCode();
        }

        @Override
        public Long copy(Long instance) {
            return instance;
        }

        @Override
        public long getQuantity(Long instance) {
            return instance;
        }

        @Override
        public Long withQuantity(Long instance, long quantity) {
            return quantity;
        }

        @Override
        public long getMaximumQuantity() {
            return Long.MAX_VALUE;
        }

        @Override
        public int conditionCompare(Boolean a, Boolean b) {
            return Boolean.compare(a, b);
        }

        @Override
        public String localize(Long instance) {
            return toString(instance);
        }

        @Override
        public MutableComponent getDisplayName(Long instance) {
            return null;
        }

        @Override
        public String toString(Long instance) {
            return String.valueOf(instance);
        }

        @Override
        public int compare(Long a, Long b) {
            return Long.compare(a, b);
        }
    }

}
