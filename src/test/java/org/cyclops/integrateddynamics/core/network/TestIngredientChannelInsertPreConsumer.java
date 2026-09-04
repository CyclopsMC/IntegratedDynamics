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
    public void testAccountingPreConsumersDoNotShareTheSameInstance() {
        AccountingPreConsumer first = new AccountingPreConsumer(1);
        AccountingPreConsumer second = new AccountingPreConsumer(1);

        long remaining = IIngredientChannelInsertPreConsumer.applyAll(
                Lists.newArrayList(first, second), MATCHER, 0, 1L, 1L, false);

        assertEquals("only the first pre-consumer accounts for the instance", 1L, first.accounted);
        assertEquals("the second pre-consumer sees nothing to account for", 0L, second.accounted);
        assertEquals("the instance is still inserted into the network", 1L, remaining);
    }

    @Test
    public void testAccountingPreConsumersSplitTheSameInstance() {
        AccountingPreConsumer first = new AccountingPreConsumer(2);
        AccountingPreConsumer second = new AccountingPreConsumer(5);

        long remaining = IIngredientChannelInsertPreConsumer.applyAll(
                Lists.newArrayList(first, second), MATCHER, 0, 3L, 3L, false);

        assertEquals(2L, first.accounted);
        assertEquals(1L, second.accounted);
        assertEquals(3L, remaining);
    }

    @Test
    public void testQuantityAccountedBeforeTheChannelIsNotAccountedAgain() {
        AccountingPreConsumer preConsumer = new AccountingPreConsumer(5);

        long remaining = IIngredientChannelInsertPreConsumer.applyAll(
                Lists.newArrayList(preConsumer), MATCHER, 0, 3L, 1L, false);

        assertEquals("only the part that was not accounted for yet can be accounted for", 1L, preConsumer.accounted);
        assertEquals(3L, remaining);
    }

    @Test
    public void testConsumedQuantityIsAlsoAccountedFor() {
        ConsumingPreConsumer first = new ConsumingPreConsumer(2);
        AccountingPreConsumer second = new AccountingPreConsumer(5);

        long remaining = IIngredientChannelInsertPreConsumer.applyAll(
                Lists.newArrayList(first, second), MATCHER, 0, 3L, 3L, false);

        assertEquals("what is consumed can not be accounted for again", 1L, second.accounted);
        assertEquals(1L, remaining);
    }

    @Test
    public void testDeprecatedPreConsumersCanNotBeOverAccounted() {
        DeprecatedConsumingPreConsumer first = new DeprecatedConsumingPreConsumer(2);
        AccountingPreConsumer second = new AccountingPreConsumer(5);

        long remaining = IIngredientChannelInsertPreConsumer.applyAll(
                Lists.newArrayList(first, second), MATCHER, 0, 3L, 3L, false);

        assertEquals("the unaccounted quantity never exceeds the remaining quantity", 1L, second.accounted);
        assertEquals(1L, remaining);
    }

    /**
     * Attributes the instance to itself without consuming it, like a pre-consumer that awaits an output.
     */
    private static class AccountingPreConsumer implements IIngredientChannelInsertPreConsumer<Long> {

        private final long capacity;
        private long accounted;

        public AccountingPreConsumer(long capacity) {
            this.capacity = capacity;
        }

        @Override
        public Result<Long> insert(int channel, Long ingredient, Long unaccounted, boolean simulate) {
            long accounting = Math.min(this.capacity - this.accounted, unaccounted);
            this.accounted += accounting;
            return new Result<>(ingredient, unaccounted - accounting);
        }
    }

    /**
     * Consumes part of the instance, so that it is not inserted into the network.
     */
    private static class ConsumingPreConsumer implements IIngredientChannelInsertPreConsumer<Long> {

        private final long capacity;

        public ConsumingPreConsumer(long capacity) {
            this.capacity = capacity;
        }

        @Override
        public Result<Long> insert(int channel, Long ingredient, Long unaccounted, boolean simulate) {
            long consumed = Math.min(this.capacity, unaccounted);
            return new Result<>(ingredient - consumed, unaccounted - consumed);
        }
    }

    /**
     * Consumes part of the instance through the deprecated api, which is unaware of accounting.
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
