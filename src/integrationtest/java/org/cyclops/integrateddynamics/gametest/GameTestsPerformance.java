package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
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
    public static final int RADIUS = 10; // Max 10, as it would otherwise leak out of the template.
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
        // Initialize empty file
        writeResults(new ArrayList<>(), false);
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = (EXECUTION_SECONDS + 10) * 20, batch = "performance_empty")
    public void testPerformanceEmptyNetwork(GameTestHelper helper) {
        testPerformance(helper, "empty", () -> CommandGenerateNetwork.NetworkGenerationHelper.generateEmptyNetwork(helper.getLevel(), helper.absolutePos(START_POS), RADIUS));
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = (EXECUTION_SECONDS + 10) * 20, batch = "performance_idle")
    public void testPerformanceIdleNetwork(GameTestHelper helper) {
        testPerformance(helper, "idle", () -> CommandGenerateNetwork.NetworkGenerationHelper.generateIdleNetwork(helper.getLevel(), helper.absolutePos(START_POS), RADIUS));
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = (EXECUTION_SECONDS + 10) * 20, batch = "performance_redstoneioclock")
    public void testPerformanceRedstoneNetwork(GameTestHelper helper) {
        testPerformance(helper, "redstoneioclock", () -> CommandGenerateNetwork.NetworkGenerationHelper.generateRedstoneNetwork(helper.getLevel(), helper.absolutePos(START_POS), RADIUS));
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = (EXECUTION_SECONDS + 10) * 20, batch = "performance_redstoneioclockvariables")
    public void testPerformanceRedstoneNetworkVariables(GameTestHelper helper) {
        testPerformance(helper, "redstoneioclock_choice", () -> CommandGenerateNetwork.NetworkGenerationHelper.generateRedstoneNetworkVariables(helper.getLevel(), helper.absolutePos(START_POS), RADIUS));
    }

    public static void testPerformance(GameTestHelper helper, String networkName, Runnable networkConstructor) {
        if (!isBenchmarkingEnabled()) {
            IntegratedDynamics.clog(Level.INFO, "Performance benchmarking disabled (PERFORMANCE_BENCHMARK_ENABLED not set)");
            helper.succeed();
            return;
        }

        ensureResultsDirectory();

        networkConstructor.run();

        // Measure the network performance
        String measurementId = networkName + "_" + System.currentTimeMillis();
        Wrapper<UUID> measurementUUID = new Wrapper<>();
        helper.runAfterDelay(200, () -> {
            // Wait a few seconds to warm up the code before starting measurement
            measurementUUID.set(NetworkDiagnostics.getInstance().startMeasurementWithoutPlayer(measurementId, EXECUTION_SECONDS));

            if (measurementUUID.get() == null) {
                throw new IllegalStateException("Failed to start measurement: " + measurementId);
            }
        });

        // Wait for measurement to complete, then retrieve results
        helper.succeedWhen(() -> {
            if (measurementUUID.get() == null || !NetworkDiagnostics.getInstance().isMeasurementComplete(measurementUUID.get())) {
                throw new GameTestAssertException("Measurement did not complete in time: " + measurementId);
            }

            double avgTickTime = NetworkDiagnostics.getInstance().getMeasurementAverageTickTime(measurementUUID.get());
            NetworkDiagnostics.getInstance().clearMeasurement(measurementUUID.get());

            List<String> results = new ArrayList<>();
            results.add(String.format("preset=%s size=%d avgTickTime=%.2f", networkName, RADIUS, avgTickTime));
            writeResults(results, true);

            CommandGenerateNetwork.NetworkGenerationHelper.clearCables(helper.getLevel(), helper.absolutePos(START_POS), RADIUS);
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
}
