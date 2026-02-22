package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.compress.utils.Lists;
import org.apache.logging.log4j.Level;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityVariablestore;
import org.cyclops.integrateddynamics.gametest.fuzzing.NetworkFuzzer;
import org.cyclops.integrateddynamics.gametest.fuzzing.NetworkFuzzerException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Fuzz testing game tests for Integrated Dynamics networks.
 * Generates random networks and verifies they do not cause crashes.
 * Enabled via the FUZZING_ENABLED environment variable.
 * Crashed networks are saved as .nbt structure files in the fuzzing_crashes/ directory.
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsFuzzing {

    public static final int MAX_NETWORK_SIZE = 100;
    public static final int MAX_PARTS = 20;
    public static final int MAX_OPERATORS = 50;
    public static final int RUN_TICKS = 50;
    public static final String TEMPLATE_EMPTY = "empty10";
    // Start at (2,1,2) to leave room for context blocks on all sides within the 10x10x10 template
    public static final BlockPos START_POS = BlockPos.ZERO.offset(2, 1, 2);

    // Cable grid dimensions: 5x5 per layer, fitting within the 10x10x10 template
    private static final int CABLE_GRID_X = 5;
    private static final int CABLE_GRID_Z = 5;

    private static final String CRASHES_DIR = "fuzzing_crashes";

    /**
     * Check if fuzzing is enabled via environment variable or system property.
     */
    private static int getFuzzingIterations() {
        String envVar = System.getenv("FUZZING_ITERATIONS");
        if (envVar != null) {
            return Integer.parseInt(System.getenv("FUZZING_ITERATIONS"));
        }
        String sysProp = System.getProperty("FUZZING_ITERATIONS");
        return sysProp != null ? Integer.parseInt(sysProp) : 0;
    }

    @GameTestGenerator
    public Collection<TestFunction> testsFuzzedNetwork() {
        List<TestFunction> testsList = Lists.newArrayList();
        int fuzzingIterations = getFuzzingIterations();
        if (fuzzingIterations == 0) {
            IntegratedDynamics.clog(Level.INFO, "[Fuzzing] Disabled (FUZZING_ITERATIONS not set to a number)");
        }
        for (int i = 0; i < fuzzingIterations; i++) {
            int finalI = i;
            testsList.add(new TestFunction(
                    "defaultBatch",
                    "test_fuzzed_network_" + i,
                    "integrateddynamics:" + TEMPLATE_EMPTY,
                    RUN_TICKS + 10,
                    1,
                    true,
                    helper -> this.testFuzzedNetwork(finalI, helper)
            ));
        }
        return testsList;
    }

//    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = RUN_TICKS + 100)
    public void testFuzzedNetwork(int gameTestIteration, GameTestHelper helper) {
        long seed = new Random().nextLong();
        Random random = new Random(seed);
        int networkSize = random.nextInt(MAX_NETWORK_SIZE) + 1;
        int numParts = random.nextInt(MAX_PARTS) + 1;
        int maxOperatorDepth = random.nextInt(MAX_OPERATORS) + 1;

        IntegratedDynamics.clog(Level.INFO, "[Fuzzing] seed=" + seed
                + ", networkSize=" + networkSize
                + ", numParts=" + numParts + ", maxOperatorDepth=" + maxOperatorDepth);

        BlockPos absStartPos = helper.absolutePos(START_POS);

        try {
            generateFuzzedNetwork(helper.getLevel(), absStartPos, networkSize, numParts, maxOperatorDepth, random);
        } catch (GameTestAssertException e) {
            throw new GameTestAssertException("[Fuzzing] " + e.getMessage());
        } catch (NetworkFuzzerException e) {
            throw new GameTestAssertException("[Fuzzing] Fuzzed network generation threw exception: " + e.getMessage());
        } catch (RuntimeException e) {
            throw e;
        } finally {
            // Always save the structure so it is available as a CI artifact for inspection.
            // This is done before runAfterDelay so the file exists even if the JVM crashes during ticking.
            String structureName = "fuzz_" + gameTestIteration + "_" + System.currentTimeMillis()
                    + "_n" + networkSize + "_p" + numParts + "_d" + maxOperatorDepth;
            try {
                saveStructure(helper.getLevel(), absStartPos, networkSize, structureName);
                IntegratedDynamics.clog(Level.INFO, "[Fuzzing] Structure saved: " + structureName + ".nbt");
            } catch (Exception e) {
                IntegratedDynamics.clog(Level.WARN, "[Fuzzing] Failed to save structure: " + e);
            }
        }


        // Let the network run for RUN_TICKS; if no crash occurs the test succeeds.
        helper.runAfterDelay(RUN_TICKS, helper::succeed);
    }

    /**
     * Generate a fuzzed network using NetworkFuzzer.
     * This method:
     * 1. Creates a cable grid
     * 2. Creates a variable store connected to the grid
     * 3. Instantiates NetworkFuzzer to generate diverse reader-operator-writer connections
     *
     * @throws NetworkFuzzerException if network generation fails
     */
    private static void generateFuzzedNetwork(net.minecraft.server.level.ServerLevel level, BlockPos startPos,
                                               int cableCount, int numParts, int maxOperatorDepth, Random random) throws NetworkFuzzerException {
        // Place cables in a compact grid (CABLE_GRID_X x CABLE_GRID_Z per layer)
        List<BlockPos> cables = new ArrayList<>();
        for (int i = 0; i < cableCount; i++) {
            int x = i % CABLE_GRID_X;
            int z = (i / CABLE_GRID_X) % CABLE_GRID_Z;
            int y = i / (CABLE_GRID_X * CABLE_GRID_Z);
            BlockPos pos = startPos.offset(x, y, z);
            level.setBlock(pos, RegistryEntries.BLOCK_CABLE.value().defaultBlockState(), 2);
            cables.add(pos);
        }

        if (cables.isEmpty()) {
            return;
        }

        // Place a variable store for intermediate variables
        BlockPos varStorePos = startPos.offset(CABLE_GRID_X + 1, 0, 0);
        level.setBlock(varStorePos, RegistryEntries.BLOCK_VARIABLE_STORE.get().defaultBlockState(), 2);

        // Connect the variable store to the cable grid with a cable
        BlockPos cableToVarStore = startPos.offset(CABLE_GRID_X, 0, 0);
        level.setBlock(cableToVarStore, RegistryEntries.BLOCK_CABLE.value().defaultBlockState(), 2);

        BlockEntityVariablestore varStore = (BlockEntityVariablestore) level.getBlockEntity(varStorePos);
        if (varStore == null) {
            throw new NetworkFuzzerException("[Fuzzing] Failed to create variable store at " + varStorePos);
        }

        // Use NetworkFuzzer to generate the network
        NetworkFuzzer fuzzer = new NetworkFuzzer(random, maxOperatorDepth, cables, varStore, level, startPos);

        // Generate multiple random connections based on numParts
        for (int i = 0; i < numParts; i++) {
            if (!fuzzer.generate()) {
                break;
            }
        }
    }


    /**
     * Save the current network region as a Minecraft structure (.nbt) file.
     * The file is written to the fuzzing_crashes/ directory so it can be uploaded as a CI artifact.
     */
    private static void saveStructure(net.minecraft.server.level.ServerLevel level, BlockPos startPos,
                                       int cableCount, String name) throws IOException {
        // Calculate the bounding box that covers the cable grid, context blocks, and variable store
        int layers = Math.max(1, (cableCount + CABLE_GRID_X * CABLE_GRID_Z - 1) / (CABLE_GRID_X * CABLE_GRID_Z));
        // Add +2 in X for variable store and context blocks, +2 in Y/Z for context blocks
        Vec3i size = new Vec3i(CABLE_GRID_X + 3, layers + 2, CABLE_GRID_Z + 2);

        StructureTemplate template = new StructureTemplate();
        // Start one block before the cable grid to capture context blocks on the WEST and NORTH sides
        BlockPos captureFrom = startPos.offset(-1, -1, -1);
        template.fillFromWorld(level, captureFrom, size, true, null);

        CompoundTag tag = template.save(new CompoundTag());
        Path crashDir = Paths.get(CRASHES_DIR);
        Files.createDirectories(crashDir);
        NbtIo.writeCompressed(tag, crashDir.resolve(name + ".nbt"));
    }
}
