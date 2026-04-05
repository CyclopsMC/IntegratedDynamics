package org.cyclops.integrateddynamics.gametest;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.logging.log4j.Level;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityVariablestore;
import org.cyclops.integrateddynamics.gametest.fuzzing.FuzzingGameTestInstance;
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
import java.util.function.BiConsumer;

/**
 * Fuzz testing game tests for Integrated Dynamics networks.
 * Generates random networks and verifies they do not cause crashes.
 * Enabled via the FUZZING_ITERATIONS environment variable.
 * Crashed networks are saved as .nbt structure files in the fuzzing_crashes/ directory.
 * @author rubensworks
 */
public class GameTestsFuzzing {

    public static final int MAX_NETWORK_SIZE = 100;
    public static final int MAX_PARTS = 20;
    public static final int MAX_OPERATORS = 50;
    public static final int RUN_TICKS = 50;
    public static final String TEMPLATE_EMPTY = "integrateddynamics:empty10";
    // Start at (2,1,2) to leave room for context blocks on all sides within the 10x10x10 template
    public static final BlockPos START_POS = BlockPos.ZERO.offset(2, 1, 2);

    // Cable grid dimensions: 5x5 per layer, fitting within the 10x10x10 template
    private static final int CABLE_GRID_X = 5;
    private static final int CABLE_GRID_Z = 5;

    private static final String CRASHES_DIR = "fuzzing_crashes";

    /**
     * Get the number of fuzzing iterations from environment variable or system property.
     * Returns 0 if not set (disabled).
     */
    private static int getFuzzingIterations() {
        String envVar = System.getenv("FUZZING_ITERATIONS");
        if (envVar != null) {
            return Integer.parseInt(envVar);
        }
        String sysProp = System.getProperty("FUZZING_ITERATIONS");
        return sysProp != null ? Integer.parseInt(sysProp) : 0;
    }

    public static void runFuzzingIteration(GameTestHelper helper, int index) {
        long seed = new Random().nextLong();
        Random random = new Random(seed);
        int networkSize = random.nextInt(MAX_NETWORK_SIZE) + 1;
        int numParts = random.nextInt(MAX_PARTS) + 1;
        int maxOperatorDepth = random.nextInt(MAX_OPERATORS) + 1;

        IntegratedDynamics.clog(Level.INFO, "[Fuzzing] iteration=" + index + " seed=" + seed
                + ", networkSize=" + networkSize
                + ", numParts=" + numParts + ", maxOperatorDepth=" + maxOperatorDepth);

        BlockPos absStartPos = helper.absolutePos(START_POS);

        try {
            generateFuzzedNetwork(helper, absStartPos, networkSize, numParts, maxOperatorDepth, random);
        } catch (RuntimeException e) {
            throw new GameTestAssertException(Component.literal("[Fuzzing] " + e.getMessage()), (int) helper.getTick());
        } catch (NetworkFuzzerException e) {
            throw new GameTestAssertException(Component.literal("[Fuzzing] Fuzzed network generation threw exception: " + e.getMessage()), (int) helper.getTick());
        } finally {
            // Always save the structure so it is available as a CI artifact for inspection.
            String structureName = "fuzz_" + index + "_" + System.currentTimeMillis()
                    + "_n" + networkSize + "_p" + numParts + "_d" + maxOperatorDepth;
            try {
                saveStructure(helper.getLevel(), absStartPos, networkSize, structureName);
                IntegratedDynamics.clog(Level.INFO, "[Fuzzing] Structure saved: " + structureName + ".nbt");
            } catch (Exception e) {
                IntegratedDynamics.clog(Level.WARN, "[Fuzzing] Failed to save structure: " + e);
            }
        }

        // Let the network run for RUN_TICKS, then proceed to the next iteration
        helper.runAfterDelay(RUN_TICKS, helper::succeed);
    }

    public static void registerCommonTests(String modId, BiConsumer<Identifier, GameTestInstance> registrar, Registry<TestEnvironmentDefinition<?>> testEnvironmentRegistry) {
        for (FuzzingGameTestInstance testInstance : fuzzingTests(modId, testEnvironmentRegistry)) {
            registrar.accept(testInstance.getId(), testInstance);
        }
    }

    public static Collection<FuzzingGameTestInstance> fuzzingTests(String modId, Registry<TestEnvironmentDefinition<?>> testEnvironmentRegistry) {
        List<FuzzingGameTestInstance> testsList = Lists.newArrayList();

        int fuzzingIterations = getFuzzingIterations();
        if (fuzzingIterations == 0) {
            IntegratedDynamics.clog(Level.INFO, "[Fuzzing] Disabled (FUZZING_ITERATIONS not set to a number)");
        }

        Holder.Reference<TestEnvironmentDefinition<?>> environment = testEnvironmentRegistry.getOrThrow(ResourceKey.create(
                Registries.TEST_ENVIRONMENT,
                Identifier.parse("default")
        ));
        for (int i = 0; i < fuzzingIterations; i++) {
            testsList.add(new FuzzingGameTestInstance(
                    new TestData<Holder<TestEnvironmentDefinition<?>>>(
                            environment,
                            Identifier.parse("integrateddynamics:test"),
                            RUN_TICKS + 10,
                            1,
                            true,
                            Rotation.NONE
                    ),
                    i));
        }

        return testsList;
    }

    /**
     * Clear blocks placed during a fuzzing iteration so the next iteration starts clean.
     */
    private static void clearArea(GameTestHelper helper, BlockPos absStartPos, int cableCount) {
        int layers = Math.max(1, (cableCount + CABLE_GRID_X * CABLE_GRID_Z - 1) / (CABLE_GRID_X * CABLE_GRID_Z));
        for (int x = -1; x < CABLE_GRID_X + 3; x++) {
            for (int y = -1; y < layers + 2; y++) {
                for (int z = -1; z < CABLE_GRID_Z + 2; z++) {
                    BlockPos pos = absStartPos.offset(x, y, z);
                    if (!helper.getLevel().isEmptyBlock(pos)) {
                        helper.getLevel().removeBlock(pos, false);
                    }
                }
            }
        }
    }

    /**
     * Generate a fuzzed network using NetworkFuzzer.
     */
    private static void generateFuzzedNetwork(GameTestHelper helper, BlockPos startPos,
                                               int cableCount, int numParts, int maxOperatorDepth, Random random) throws NetworkFuzzerException {
        net.minecraft.server.level.ServerLevel level = helper.getLevel();

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
        NetworkFuzzer fuzzer = new NetworkFuzzer(helper, random, maxOperatorDepth, cables, varStore, level, startPos);

        // Generate multiple random connections based on numParts
        for (int i = 0; i < numParts; i++) {
            if (!fuzzer.generate()) {
                break;
            }
        }
    }

    /**
     * Save the current network region as a Minecraft structure (.nbt) file.
     */
    private static void saveStructure(net.minecraft.server.level.ServerLevel level, BlockPos startPos,
                                       int cableCount, String name) throws IOException {
        int layers = Math.max(1, (cableCount + CABLE_GRID_X * CABLE_GRID_Z - 1) / (CABLE_GRID_X * CABLE_GRID_Z));
        Vec3i size = new Vec3i(CABLE_GRID_X + 3, layers + 2, CABLE_GRID_Z + 2);

        StructureTemplate template = new StructureTemplate();
        BlockPos captureFrom = startPos.offset(-1, -1, -1);
        template.fillFromWorld(level, captureFrom, size, true, List.of());

        CompoundTag tag = template.save(new CompoundTag());
        Path crashDir = Paths.get(CRASHES_DIR);
        Files.createDirectories(crashDir);
        NbtIo.writeCompressed(tag, crashDir.resolve(name + ".nbt"));
    }
}
