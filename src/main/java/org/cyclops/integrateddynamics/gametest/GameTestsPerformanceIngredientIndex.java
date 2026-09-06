package org.cyclops.integrateddynamics.gametest;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import org.apache.logging.log4j.Level;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.gametest.GameTest;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PrioritizedPartPos;
import org.cyclops.integrateddynamics.core.network.IngredientPositionsIndex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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
public class GameTestsPerformanceIngredientIndex {

    public static final String TEMPLATE_EMPTY = "integrateddynamics:empty10";

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

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 6000, environment = Reference.MOD_ID + ":performance_index")
    public void testPerformanceIndexLookupExact(GameTestHelper helper) {
        // The lookup that is performed when a specific instance is extracted from a storage network
        benchmark(helper, "index_lookup_exact", OPERATIONS, fixture ->
                i -> fixture.countPositions(fixture.instance(i), IngredientComponent.ITEMSTACK.getMatcher()
                        .getExactMatchNoQuantityCondition()));
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 6000, environment = Reference.MOD_ID + ":performance_index")
    public void testPerformanceIndexLookupItem(GameTestHelper helper) {
        // The lookup that is performed when data components are to be ignored during extraction
        benchmark(helper, "index_lookup_item", OPERATIONS, fixture ->
                i -> fixture.countPositions(fixture.instance(i), ItemMatch.ITEM));
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 6000, environment = Reference.MOD_ID + ":performance_index")
    public void testPerformanceIndexLookupNonEmptyFirst(GameTestHelper helper) {
        // Quantity-based extractions iterate over the non-empty positions,
        // and stop as soon as a usable position is found.
        benchmark(helper, "index_lookup_nonempty_first", OPERATIONS, fixture ->
                i -> fixture.getIndex().getNonEmptyPositions().hasNext() ? 1 : 0);
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 6000, environment = Reference.MOD_ID + ":performance_index")
    public void testPerformanceIndexLookupNonEmptyAll(GameTestHelper helper) {
        // Worst case for the non-empty position lookup: all positions are consumed
        benchmark(helper, "index_lookup_nonempty_all", OPERATIONS / 100, fixture ->
                i -> count(fixture.getIndex().getNonEmptyPositions()));
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 6000, environment = Reference.MOD_ID + ":performance_index")
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

    // The benchmarks below repeat the two hottest operations over collision-heavy shapes.
    // They are the cases that a component-blind ItemStack hash makes quadratic.

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 6000, environment = Reference.MOD_ID + ":performance_index")
    public void testPerformanceIndexLookupExactSingleItem(GameTestHelper helper) {
        benchmark(helper, "index_lookup_exact", OPERATIONS, Shape.SINGLE_ITEM, fixture ->
                i -> fixture.countPositions(fixture.instance(i), IngredientComponent.ITEMSTACK.getMatcher()
                        .getExactMatchNoQuantityCondition()));
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 6000, environment = Reference.MOD_ID + ":performance_index")
    public void testPerformanceIndexModificationSingleItem(GameTestHelper helper) {
        benchmark(helper, "index_modification", OPERATIONS, Shape.SINGLE_ITEM, fixture -> i -> {
            ItemStack instance = fixture.instance(i);
            PrioritizedPartPos pos = fixture.positionOf(i);
            fixture.getIndex().removePosition(instance, pos);
            fixture.getIndex().addPosition(instance, pos);
            return 1;
        });
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 6000, environment = Reference.MOD_ID + ":performance_index")
    public void testPerformanceIndexLookupExactFewItems(GameTestHelper helper) {
        benchmark(helper, "index_lookup_exact", OPERATIONS, Shape.FEW_ITEMS, fixture ->
                i -> fixture.countPositions(fixture.instance(i), IngredientComponent.ITEMSTACK.getMatcher()
                        .getExactMatchNoQuantityCondition()));
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 6000, environment = Reference.MOD_ID + ":performance_index")
    public void testPerformanceIndexModificationFewItems(GameTestHelper helper) {
        benchmark(helper, "index_modification", OPERATIONS, Shape.FEW_ITEMS, fixture -> i -> {
            ItemStack instance = fixture.instance(i);
            PrioritizedPartPos pos = fixture.positionOf(i);
            fixture.getIndex().removePosition(instance, pos);
            fixture.getIndex().addPosition(instance, pos);
            return 1;
        });
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 6000, environment = Reference.MOD_ID + ":performance_index")
    public void testPerformanceIndexLookupExactHeavyComponents(GameTestHelper helper) {
        // Guards the opposite risk: a hash that includes components costs more per call
        benchmark(helper, "index_lookup_exact", OPERATIONS, Shape.HEAVY_COMPONENTS, fixture ->
                i -> fixture.countPositions(fixture.instance(i), IngredientComponent.ITEMSTACK.getMatcher()
                        .getExactMatchNoQuantityCondition()));
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 6000, environment = Reference.MOD_ID + ":performance_index")
    public void testPerformanceIndexModificationHeavyComponents(GameTestHelper helper) {
        benchmark(helper, "index_modification", OPERATIONS, Shape.HEAVY_COMPONENTS, fixture -> i -> {
            ItemStack instance = fixture.instance(i);
            PrioritizedPartPos pos = fixture.positionOf(i);
            fixture.getIndex().removePosition(instance, pos);
            fixture.getIndex().addPosition(instance, pos);
            return 1;
        });
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 6000, environment = Reference.MOD_ID + ":performance_index")
    public void testPerformanceIndexLookupExactPlain(GameTestHelper helper) {
        benchmark(helper, "index_lookup_exact", OPERATIONS, Shape.PLAIN, fixture ->
                i -> fixture.countPositions(fixture.instance(i), IngredientComponent.ITEMSTACK.getMatcher()
                        .getExactMatchNoQuantityCondition()));
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 6000, environment = Reference.MOD_ID + ":performance_index")
    public void testPerformanceIndexLookupItemPlain(GameTestHelper helper) {
        benchmark(helper, "index_lookup_item", OPERATIONS, Shape.PLAIN, fixture ->
                i -> fixture.countPositions(fixture.instance(i), ItemMatch.ITEM));
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 6000, environment = Reference.MOD_ID + ":performance_index")
    public void testPerformanceIndexModificationPlain(GameTestHelper helper) {
        benchmark(helper, "index_modification", OPERATIONS, Shape.PLAIN, fixture -> i -> {
            ItemStack instance = fixture.instance(i);
            PrioritizedPartPos pos = fixture.positionOf(i);
            fixture.getIndex().removePosition(instance, pos);
            fixture.getIndex().addPosition(instance, pos);
            return 1;
        });
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = 6000, environment = Reference.MOD_ID + ":performance_index")
    public void testPerformanceIndexLookupItemSingleItem(GameTestHelper helper) {
        benchmark(helper, "index_lookup_item", OPERATIONS, Shape.SINGLE_ITEM, fixture ->
                i -> fixture.countPositions(fixture.instance(i), ItemMatch.ITEM));
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
        benchmark(helper, name, operations, Shape.SPREAD, operationFactory);
    }

    protected static void benchmark(GameTestHelper helper, String name, int operations, Shape shape,
                                    Function<Fixture, IntUnaryOperator> operationFactory) {
        name = name + shape.benchmarkSuffix();
        if (!GameTestsPerformance.isBenchmarkingEnabled()) {
            IntegratedDynamics.clog(Level.INFO, "Performance benchmarking disabled (PERFORMANCE_BENCHMARK_ENABLED not set)");
            helper.succeed();
            return;
        }

        GameTestsPerformance.ensureResultsDirectory();

        double[] operationTimes = new double[ROUNDS];
        for (int round = 0; round < ROUNDS; round++) {
            IntUnaryOperator operation = operationFactory.apply(new Fixture(helper, shape));

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
     * How the indexed instances are spread over item types.
     *
     * An ItemStack hash that ignores data components puts every stack of the same item in one
     * bucket, so the shape decides how long the collision chains are. SPREAD is the mildest case
     * and SINGLE_ITEM the worst.
     */
    public enum Shape {
        /**
         * Spread over every registered item, with component variants to reach the target size.
         */
        SPREAD,
        /**
         * Every instance on one item, distinct only by data components.
         * This is what a storage full of enchanted books or damaged tools looks like.
         */
        SINGLE_ITEM,
        /**
         * Spread over a few item types only, distinct by data components within each.
         */
        FEW_ITEMS,
        /**
         * Spread as SPREAD, but each stack carries a large component payload,
         * so that the cost of hashing the components themselves is visible.
         */
        HEAVY_COMPONENTS,
        /**
         * Every instance is a plain stack carrying no data components at all, made distinct by its
         * item and its count. This is what a large modpack's storage mostly looks like: many unique
         * items, few component variants.
         */
        PLAIN;

        public String benchmarkSuffix() {
            return this == SPREAD ? "" : "_" + name().toLowerCase(Locale.ROOT);
        }
    }

    /**
     * The number of distinct item types used by {@link Shape#FEW_ITEMS}.
     */
    public static final int FEW_ITEMS_COUNT = 50;

    /**
     * An index that is filled with a large number of instances, spread over a large number of positions.
     */
    protected static class Fixture {

        private final IngredientPositionsIndex<ItemStack, Integer> index;
        private final List<ItemStack> instances;
        private final List<PrioritizedPartPos> positions;

        public Fixture(GameTestHelper helper) {
            this(helper, Shape.SPREAD);
        }

        public Fixture(GameTestHelper helper, Shape shape) {
            this.index = new IngredientPositionsIndex<>(IngredientComponent.ITEMSTACK);
            this.instances = new ArrayList<>(INSTANCES);
            this.positions = new ArrayList<>(POSITIONS);

            for (int i = 0; i < POSITIONS; i++) {
                PartPos partPos = PartPos.of(DimPos.of(helper.getLevel(),
                        helper.absolutePos(new BlockPos(i % 10, 1 + (i / 10) % 10, 1))), Direction.values()[i % 6]);
                this.positions.add(PrioritizedPartPos.of(partPos, i % 4));
            }

            // Air has to be excluded: a stack of it is empty, and an empty stack carries no
            // components, so a fixture built on it would index nothing.
            List<Item> items = BuiltInRegistries.ITEM.stream()
                    .filter(item -> item != Items.AIR)
                    .toList();
            for (int i = 0; i < INSTANCES; i++) {
                ItemStack instance = createInstance(shape, items, i);
                this.instances.add(instance);
                this.index.addPosition(instance, positionOf(i));
            }
        }

        private static ItemStack createInstance(Shape shape, List<Item> items, int i) {
            switch (shape) {
                case SINGLE_ITEM -> {
                    ItemStack instance = new ItemStack(items.get(0));
                    instance.set(DataComponents.CUSTOM_NAME, Component.literal("Variant " + i));
                    return instance;
                }
                case FEW_ITEMS -> {
                    ItemStack instance = new ItemStack(items.get(i % FEW_ITEMS_COUNT));
                    instance.set(DataComponents.CUSTOM_NAME, Component.literal("Variant " + (i / FEW_ITEMS_COUNT)));
                    return instance;
                }
                case HEAVY_COMPONENTS -> {
                    ItemStack instance = new ItemStack(items.get(i % items.size()));
                    instance.set(DataComponents.CUSTOM_NAME, Component.literal("Variant " + (i / items.size())));
                    instance.set(DataComponents.LORE, new ItemLore(List.of(
                            Component.literal("Payload line one for entry " + i),
                            Component.literal("Payload line two for entry " + i),
                            Component.literal("Payload line three for entry " + i))));
                    List<ItemStack> contained = new ArrayList<>(4);
                    for (int c = 0; c < 4; c++) {
                        contained.add(new ItemStack(items.get((i + c) % items.size()), 1 + c));
                    }
                    instance.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contained));
                    return instance;
                }
                case PLAIN -> {
                    // Distinct by item and count only, so that no stack carries a component patch
                    return new ItemStack(items.get(i % items.size()), 1 + i / items.size());
                }
                default -> {
                    ItemStack instance = new ItemStack(items.get(i % items.size()));
                    if (i >= items.size()) {
                        instance.set(DataComponents.CUSTOM_NAME, Component.literal("Variant " + (i / items.size())));
                    }
                    return instance;
                }
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
