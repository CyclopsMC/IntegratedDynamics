package org.cyclops.integrateddynamics.gametest;

import com.google.common.math.Stats;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.TimeUtil;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.command.CommandGenerateNetwork;
import org.cyclops.integrateddynamics.core.network.diagnostics.NetworkDiagnostics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Game tests for performance benchmarking of network operations.
 * These tests generate networks with different presets and measure their performance.
 * Results are written to runs/gameTestServer/logs/benchmark_results.txt for CI processing.
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsPerformance {

    public static final int EXECUTION_SECONDS = 10;
    public static final int WARMUP_TICKS = 200;
    public static final int RADIUS = 10; // Max 10, as it would otherwise leak out of the template.
    public static final int APPEND_CABLE_COUNT = 100;
    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos START_POS = BlockPos.ZERO.offset(1, 1, 1);

    private static final String RESULTS_FILE = "logs/benchmark_results.txt";

    /**
     * Check if performance benchmarking is enabled via environment variable.
     *
     * @return true if PERFORMANCE_BENCHMARK_ENABLED environment variable is set to "true"
     */
    private static boolean isBenchmarkingEnabled() {
        // Check environment variable first
        String envVar = System.getenv("PERFORMANCE_BENCHMARK_ENABLED");
        if (envVar != null && "true".equalsIgnoreCase(envVar)) {
            return true;
        }

        // Check system property as fallback
        String sysProp = System.getProperty("PERFORMANCE_BENCHMARK_ENABLED");
        return sysProp != null && "true".equalsIgnoreCase(sysProp);
    }

    static {
        if (isBenchmarkingEnabled()) {
            // Initialize empty file
            writeResults(new ArrayList<>(), false);
        }
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = (EXECUTION_SECONDS + 10) * 20, batch = "performance_empty")
    public void testPerformanceEmptyNetwork(GameTestHelper helper) {
        testPerformance(helper, "empty", (measureServerTickTimeNow) -> CommandGenerateNetwork.NetworkGenerationHelper.generateEmptyNetwork(helper.getLevel(), helper.absolutePos(START_POS), RADIUS));
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = (EXECUTION_SECONDS + 10) * 20, batch = "performance_idle")
    public void testPerformanceIdleNetwork(GameTestHelper helper) {
        testPerformance(helper, "idle", (measureServerTickTimeNow) -> CommandGenerateNetwork.NetworkGenerationHelper.generateIdleNetwork(helper.getLevel(), helper.absolutePos(START_POS), RADIUS));
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = (EXECUTION_SECONDS + 10) * 20, batch = "performance_redstoneioclock")
    public void testPerformanceRedstoneNetwork(GameTestHelper helper) {
        testPerformance(helper, "redstoneioclock", (measureServerTickTimeNow) -> CommandGenerateNetwork.NetworkGenerationHelper.generateRedstoneNetwork(helper.getLevel(), helper.absolutePos(START_POS), RADIUS));
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = (EXECUTION_SECONDS + 10) * 20, batch = "performance_redstoneioclockvariables")
    public void testPerformanceRedstoneNetworkVariables(GameTestHelper helper) {
        testPerformance(helper, "redstoneioclock_choice", (measureServerTickTimeNow) -> CommandGenerateNetwork.NetworkGenerationHelper.generateRedstoneNetworkVariables(helper.getLevel(), helper.absolutePos(START_POS), RADIUS));
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = (EXECUTION_SECONDS + 10) * 20, batch = "performance_empty_append")
    public void testPerformanceEmptyNetworkAppend(GameTestHelper helper) {
        testPerformance(helper, "empty_append", (measureServerTickTimeNow) -> {
            CommandGenerateNetwork.NetworkGenerationHelper.generateEmptyNetwork(helper.getLevel(), helper.absolutePos(START_POS), RADIUS);
            addCablesPostWarmup(helper, WARMUP_TICKS);
            helper.runAfterDelay(WARMUP_TICKS + 100, measureServerTickTimeNow); // Measure server tick time right after cables have been added
        });
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = (EXECUTION_SECONDS + 10) * 20, batch = "performance_redstoneioclock_append")
    public void testPerformanceIdleNetworkAppend(GameTestHelper helper) {
        testPerformance(helper, "redstoneioclock_append", (measureServerTickTimeNow) -> {
            CommandGenerateNetwork.NetworkGenerationHelper.generateRedstoneNetwork(helper.getLevel(), helper.absolutePos(START_POS), RADIUS);
            addCablesPostWarmup(helper, WARMUP_TICKS);
            helper.runAfterDelay(WARMUP_TICKS + 100, measureServerTickTimeNow); // Measure server tick time right after cables have been added
        });
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = (EXECUTION_SECONDS + 10) * 20, batch = "performance_empty_remove")
    public void testPerformanceEmptyNetworkRemove(GameTestHelper helper) {
        testPerformance(helper, "empty_remove", (measureServerTickTimeNow) -> {
            CommandGenerateNetwork.NetworkGenerationHelper.generateEmptyNetwork(helper.getLevel(), helper.absolutePos(START_POS), RADIUS);
            removeCablesPostWarmup(helper, RADIUS, 100, WARMUP_TICKS);
            helper.runAfterDelay(WARMUP_TICKS + 100, measureServerTickTimeNow); // Measure server tick time right after cables have been removed
        });
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = (EXECUTION_SECONDS + 10) * 20, batch = "performance_redstoneioclock_remove")
    public void testPerformanceRedstoneNetworkRemove(GameTestHelper helper) {
        testPerformance(helper, "redstoneioclock_remove", (measureServerTickTimeNow) -> {
            CommandGenerateNetwork.NetworkGenerationHelper.generateRedstoneNetwork(helper.getLevel(), helper.absolutePos(START_POS), RADIUS);
            removeCablesPostWarmup(helper, RADIUS, 100, WARMUP_TICKS);
            helper.runAfterDelay(WARMUP_TICKS + 100, measureServerTickTimeNow); // Measure server tick time right after cables have been removed
        });
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = (EXECUTION_SECONDS + 10) * 20, batch = "performance_empty_appendparts")
    public void testPerformanceEmptyNetworkAppendParts(GameTestHelper helper) {
        testPerformance(helper, "empty_appendparts", (measureServerTickTimeNow) -> {
            CommandGenerateNetwork.NetworkGenerationHelper.generateEmptyNetwork(helper.getLevel(), helper.absolutePos(START_POS), RADIUS);
            addPartsPostWarmup(helper, RADIUS, 100, WARMUP_TICKS);
            helper.runAfterDelay(WARMUP_TICKS + 100, measureServerTickTimeNow); // Measure server tick time right after parts have been added
        });
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = (EXECUTION_SECONDS + 10) * 20, batch = "performance_redstoneioclock_appendparts")
    public void testPerformanceRedstoneNetworkAppendParts(GameTestHelper helper) {
        testPerformance(helper, "redstoneioclock_appendparts", (measureServerTickTimeNow) -> {
            CommandGenerateNetwork.NetworkGenerationHelper.generateRedstoneNetwork(helper.getLevel(), helper.absolutePos(START_POS), RADIUS);
            addPartsPostWarmup(helper, RADIUS, 100, WARMUP_TICKS);
            helper.runAfterDelay(WARMUP_TICKS + 100, measureServerTickTimeNow); // Measure server tick time right after parts have been added
        });
    }

    public static void testPerformance(GameTestHelper helper, String networkName, Consumer<Runnable> networkConstructor) {
        if (!isBenchmarkingEnabled()) {
            IntegratedDynamics.clog(Level.INFO, "Performance benchmarking disabled (PERFORMANCE_BENCHMARK_ENABLED not set)");
            helper.succeed();
            return;
        }

        ensureResultsDirectory();

        // Calculate average server-wide tick time
        Wrapper<Double> avgServerTickTime = new Wrapper<>(0D);
        Runnable measureServerTickTimeNow = () -> avgServerTickTime.set(Stats.meanOf(helper.getLevel().getServer().getTickTimesNanos()) / TimeUtil.NANOSECONDS_PER_MILLISECOND);
        networkConstructor.accept(measureServerTickTimeNow);

        // Measure the network performance
        String measurementId = networkName + "_" + System.currentTimeMillis();
        Wrapper<UUID> measurementUUID = new Wrapper<>();
        helper.runAfterDelay(WARMUP_TICKS, () -> {
            // Wait a few seconds to warm up the code before starting measurement
            measurementUUID.set(NetworkDiagnostics.getInstance().startMeasurementWithoutPlayer(measurementId, EXECUTION_SECONDS));
        });

        // Wait for measurement to complete, then retrieve results
        helper.succeedWhen(() -> {
            if (measurementUUID.get() == null || !NetworkDiagnostics.getInstance().isMeasurementComplete(measurementUUID.get())) {
                throw new GameTestAssertException("Measurement did not complete in time: " + measurementId);
            }

            double avgTickTime = NetworkDiagnostics.getInstance().getMeasurementAverageTickTime(measurementUUID.get());
            NetworkDiagnostics.getInstance().clearMeasurement(measurementUUID.get());

            // Calculate average server-wide tick time
            if (avgServerTickTime.get() == 0D) {
                measureServerTickTimeNow.run();
            }

            List<String> results = new ArrayList<>();
            results.add(String.format("preset=%s size=%d avgNetworkTickTime=%.2f avgServerTickTime=%.2f", networkName, RADIUS, avgTickTime, avgServerTickTime.get()));
            writeResults(results, true);

            CommandGenerateNetwork.NetworkGenerationHelper.clearCables(helper.getLevel(), helper.absolutePos(START_POS), RADIUS);
            clearAppendedCables(helper);
        });
    }

    private static void ensureResultsDirectory() {
        try {
            Files.createDirectories(Paths.get("logs"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static synchronized void writeResults(List<String> results, boolean append) {
        try {
            String content = String.join("\n", results);
            if (append && Files.exists(Paths.get(RESULTS_FILE))) {
                String existingString = Files.readString(Paths.get(RESULTS_FILE));
                content = (existingString.isEmpty() ? content : existingString + content) + "\n";
            }
            Files.write(Paths.get(RESULTS_FILE), content.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The position of the cable that {@link #addCablesPostWarmup} appends to the network at the given index.
     * Note that these are placed outside of the generated network cube.
     */
    private static BlockPos getAppendedCablePos(GameTestHelper helper, int index) {
        return helper.absolutePos(START_POS).offset(0, index, - 1);
    }

    private static void addCablesPostWarmup(GameTestHelper helper, int delayOffset) {
        for (int i = 0; i < APPEND_CABLE_COUNT; i++) {
            final int index = i;
            helper.runAfterDelay(delayOffset + i, () -> {
                // Add a cable at the correct position
                CommandGenerateNetwork.NetworkGenerationHelper.placeCable(helper.getLevel(), getAppendedCablePos(helper, index));
            });
        }
    }

    /**
     * Remove the cables that {@link #addCablesPostWarmup} appended to the network.
     *
     * These are placed outside of the generated network cube,
     * so they are not covered by the regular cleanup of that cube.
     * Leaving them behind would keep their network alive for the remainder of the server run,
     * which distorts the measurements of all tests that run afterwards,
     * and would persist into subsequent runs that reuse the same world.
     */
    private static void clearAppendedCables(GameTestHelper helper) {
        CommandGenerateNetwork.NetworkGenerationHelper.clearCables(helper.getLevel(),
                getAppendedCablePos(helper, 0), getAppendedCablePos(helper, APPEND_CABLE_COUNT - 1));
    }

    private static void removeCablesPostWarmup(GameTestHelper helper, int radius, int count, int delayOffset) {
        for (int i = 0; i < count; i++) {
            final int index = i;
            helper.runAfterDelay(delayOffset + i, () -> helper.destroyBlock(START_POS.offset(index / radius, index % radius, 0)));
        }
    }

    private static void addPartsPostWarmup(GameTestHelper helper, int radius, int count, int delayOffset) {
        for (int i = 0; i < count; i++) {
            final int index = i;
            helper.runAfterDelay(delayOffset + i, () -> {
                // Add a part to NORTH-facing cable at the correct position
                BlockPos pos = helper.absolutePos(START_POS).offset(index / radius, index % radius, 0);
                CommandGenerateNetwork.NetworkGenerationHelper.addPartToNorthFace(helper.getLevel(), pos);
            });
        }
    }
}
