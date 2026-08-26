package org.cyclops.integrateddynamics.gametest;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.apache.logging.log4j.Level;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PrioritizedPartPos;
import org.cyclops.integrateddynamics.core.network.IngredientPositionsIndex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

/**
 * Performance benchmarks for {@link IngredientPositionsIndex}.
 *
 * These measure the index lookups that storage networks perform on every insertion and extraction.
 * This is not covered by the network tick time benchmarks in {@link GameTestsPerformance},
 * as those do not contain any storage positions.
 * Results are written to runs/gameTestServer/logs/benchmark_results.txt for CI processing.
 *
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsPerformanceIngredientIndex {

    public static final String TEMPLATE_EMPTY = "empty10";

    /**
     * The number of distinct instances that is indexed.
     */
    public static final int INSTANCES = 5_000;
    /**
     * The number of positions over which the instances are spread.
     */
    public static final int POSITIONS = 200;
    /**
     * The number of measured operations per benchmark.
     */
    public static final int OPERATIONS = 20_000;
    /**
     * The number of unmeasured operations that are executed before each benchmark, to warm up the JIT.
     */
    public static final int WARMUP_OPERATIONS = 5_000;
    /**
     * The number of times each benchmark is repeated, of which the median is reported.
     * Repeating reduces the effect of garbage collections during a single measurement.
     */
    public static final int ROUNDS = 3;

    static {
        // Make sure that the results file is initialized by GameTestsPerformance before we append to it.
        GameTestsPerformance.isBenchmarkingEnabled();
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 6000, batch = "performance_index")
    public void testPerformanceIndexLookupExact(GameTestHelper helper) {
        // The lookup that is performed when a specific instance is extracted from a storage network
        benchmark(helper, "index_lookup_exact", OPERATIONS, fixture ->
                i -> fixture.countPositions(fixture.instance(i), IngredientComponent.ITEMSTACK.getMatcher()
                        .getExactMatchNoQuantityCondition()));
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 6000, batch = "performance_index")
    public void testPerformanceIndexLookupItem(GameTestHelper helper) {
        // The lookup that is performed when data components are to be ignored during extraction
        benchmark(helper, "index_lookup_item", OPERATIONS, fixture ->
                i -> fixture.countPositions(fixture.instance(i), ItemMatch.ITEM));
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 6000, batch = "performance_index")
    public void testPerformanceIndexLookupNonEmptyFirst(GameTestHelper helper) {
        // Quantity-based extractions iterate over the non-empty positions,
        // and stop as soon as a usable position is found.
        benchmark(helper, "index_lookup_nonempty_first", OPERATIONS, fixture ->
                i -> fixture.getIndex().getNonEmptyPositions().hasNext() ? 1 : 0);
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 6000, batch = "performance_index")
    public void testPerformanceIndexLookupNonEmptyAll(GameTestHelper helper) {
        // Worst case for the non-empty position lookup: all positions are consumed
        benchmark(helper, "index_lookup_nonempty_all", OPERATIONS / 100, fixture ->
                i -> count(fixture.getIndex().getNonEmptyPositions()));
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 6000, batch = "performance_index")
    public void testPerformanceIndexModification(GameTestHelper helper) {
        // Index updates, as performed when the contents of a storage position change
        benchmark(helper, "index_modification", OPERATIONS, fixture -> i -> {
            ItemStack instance = fixture.instance(i);
            PrioritizedPartPos pos = fixture.positionOf(i);
            fixture.getIndex().removePosition(instance, pos);
            fixture.getIndex().addPosition(instance, pos);
            return 1;
        });
    }

    protected static int count(Iterator<?> iterator) {
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        return count;
    }

    /**
     * Run the given operation on a freshly constructed index,
     * and append its average execution time to the benchmark results.
     *
     * @param helper A game test helper.
     * @param name The benchmark name.
     * @param operations The number of measured operations.
     * @param operationFactory A factory for the operation to measure, taking an operation index.
     */
    protected static void benchmark(GameTestHelper helper, String name, int operations,
                                    Function<Fixture, IntUnaryOperator> operationFactory) {
        if (!GameTestsPerformance.isBenchmarkingEnabled()) {
            IntegratedDynamics.clog(Level.INFO, "Performance benchmarking disabled (PERFORMANCE_BENCHMARK_ENABLED not set)");
            helper.succeed();
            return;
        }

        GameTestsPerformance.ensureResultsDirectory();

        double[] operationTimes = new double[ROUNDS];
        for (int round = 0; round < ROUNDS; round++) {
            IntUnaryOperator operation = operationFactory.apply(new Fixture(helper));

            // Accumulate all operation results, so that they can not be optimized away
            long checksum = 0;
            for (int i = 0; i < Math.min(WARMUP_OPERATIONS, operations * 2); i++) {
                checksum += operation.applyAsInt(i);
            }

            // Collect the garbage of the warmup phase, so that it can't be attributed to the measurement
            System.gc();

            long start = System.nanoTime();
            for (int i = 0; i < operations; i++) {
                checksum += operation.applyAsInt(i);
            }
            operationTimes[round] = ((double) (System.nanoTime() - start) / operations) / 1_000_000D;

            if (checksum == Long.MIN_VALUE) {
                throw new IllegalStateException("Unreachable");
            }
        }
        Arrays.sort(operationTimes);

        double averageOperationTime = operationTimes[ROUNDS / 2];
        IntegratedDynamics.clog(Level.INFO, String.format("Benchmark %s: %.6f ms/op (min: %.6f, max: %.6f)",
                name, averageOperationTime, operationTimes[0], operationTimes[ROUNDS - 1]));
        GameTestsPerformance.writeResults(Lists.newArrayList(String.format(
                "preset=%s size=%d avgOperationTime=%.6f", name, INSTANCES, averageOperationTime)), true);

        helper.succeed();
    }

    /**
     * An index that is filled with a large number of instances, spread over a large number of positions.
     */
    protected static class Fixture {

        private final IngredientPositionsIndex<ItemStack, Integer> index;
        private final List<ItemStack> instances;
        private final List<PrioritizedPartPos> positions;

        public Fixture(GameTestHelper helper) {
            this.index = new IngredientPositionsIndex<>(IngredientComponent.ITEMSTACK);
            this.instances = new ArrayList<>(INSTANCES);
            this.positions = new ArrayList<>(POSITIONS);

            for (int i = 0; i < POSITIONS; i++) {
                PartPos partPos = PartPos.of(DimPos.of(helper.getLevel(),
                        helper.absolutePos(new BlockPos(i % 10, 1 + (i / 10) % 10, 1))), Direction.values()[i % 6]);
                this.positions.add(PrioritizedPartPos.of(partPos, i % 4));
            }

            // Create instances based on all registered items,
            // with additional data component variants to reach the target size.
            List<Item> items = BuiltInRegistries.ITEM.stream().toList();
            for (int i = 0; i < INSTANCES; i++) {
                ItemStack instance = new ItemStack(items.get(i % items.size()));
                if (i >= items.size()) {
                    instance.set(DataComponents.CUSTOM_NAME, Component.literal("Variant " + (i / items.size())));
                }
                this.instances.add(instance);
                this.index.addPosition(instance, positionOf(i));
            }
        }

        public IngredientPositionsIndex<ItemStack, Integer> getIndex() {
            return this.index;
        }

        public ItemStack instance(int i) {
            return this.instances.get(Math.floorMod(i, INSTANCES));
        }

        public PrioritizedPartPos positionOf(int i) {
            return this.positions.get(Math.floorMod(i, INSTANCES) % POSITIONS);
        }

        public int countPositions(ItemStack instance, int matchCondition) {
            return count(this.index.getPositions(instance, matchCondition));
        }
    }

}
