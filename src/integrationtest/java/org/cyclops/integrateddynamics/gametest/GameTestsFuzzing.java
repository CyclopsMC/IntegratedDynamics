package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.blockentity.BlockEntityVariablestore;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
    public static final int RUN_TICKS = 200;
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
    private static boolean isFuzzingEnabled() {
        String envVar = System.getenv("FUZZING_ENABLED");
        if (envVar != null && "true".equalsIgnoreCase(envVar)) {
            return true;
        }
        String sysProp = System.getProperty("FUZZING_ENABLED");
        return sysProp != null && "true".equalsIgnoreCase(sysProp);
    }

    @GameTest(template = TEMPLATE_EMPTY, timeoutTicks = RUN_TICKS + 100)
    public void testFuzzedNetwork(GameTestHelper helper) {
        if (!isFuzzingEnabled()) {
            IntegratedDynamics.clog(Level.INFO, "[Fuzzing] Disabled (FUZZING_ENABLED not set)");
            helper.succeed();
            return;
        }

        Random random = new Random();
        int networkSize = 1 + random.nextInt(MAX_NETWORK_SIZE);
        int numParts = random.nextInt(MAX_PARTS + 1);
        int operatorCount = random.nextInt(MAX_OPERATORS + 1);

        IntegratedDynamics.clog(Level.INFO, "[Fuzzing] networkSize=" + networkSize
                + ", numParts=" + numParts + ", operatorCount=" + operatorCount);

        BlockPos absStartPos = helper.absolutePos(START_POS);
        Wrapper<Throwable> generationError = new Wrapper<>(null);

        try {
            generateFuzzedNetwork(helper.getLevel(), absStartPos, networkSize, numParts, operatorCount, random);
        } catch (Throwable e) {
            generationError.set(e);
            IntegratedDynamics.clog(Level.ERROR, "[Fuzzing] Exception during network generation: " + e);
        }

        // Always save the structure so it is available as a CI artifact for inspection.
        // This is done before runAfterDelay so the file exists even if the JVM crashes during ticking.
        String structureName = "fuzz_" + System.currentTimeMillis()
                + "_n" + networkSize + "_p" + numParts + "_o" + operatorCount;
        try {
            saveStructure(helper.getLevel(), absStartPos, networkSize, structureName);
            IntegratedDynamics.clog(Level.INFO, "[Fuzzing] Structure saved: " + structureName + ".nbt");
        } catch (Exception e) {
            IntegratedDynamics.clog(Level.WARN, "[Fuzzing] Failed to save structure: " + e);
        }

        if (generationError.get() != null) {
            throw new GameTestAssertException("Fuzzed network generation threw exception: "
                    + generationError.get().getMessage());
        }

        // Let the network run for RUN_TICKS; if no crash occurs the test succeeds.
        helper.runAfterDelay(RUN_TICKS, helper::succeed);
    }

    /**
     * Generate a fuzzed network: cables, redstone readers, redstone writers,
     * and various operator-based logic connections between them.
     */
    private static void generateFuzzedNetwork(net.minecraft.server.level.ServerLevel level, BlockPos startPos,
                                               int cableCount, int numParts, int maxOperators, Random random) {
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

        // Find outer faces (adjacent to non-cable blocks)
        Set<BlockPos> cableSet = new HashSet<>(cables);
        List<Pair<BlockPos, Direction>> outerFaces = new ArrayList<>();
        for (BlockPos cable : cables) {
            for (Direction dir : Direction.values()) {
                if (!cableSet.contains(cable.relative(dir))) {
                    outerFaces.add(Pair.of(cable, dir));
                }
            }
        }
        Collections.shuffle(outerFaces, random);

        // Split outer faces into reader and writer positions
        int totalParts = Math.min(numParts, outerFaces.size());
        int readerCount = totalParts / 2;
        int writerCount = totalParts - readerCount;

        List<Pair<BlockPos, Direction>> readerFaces = new ArrayList<>(outerFaces.subList(0, readerCount));
        List<Pair<BlockPos, Direction>> writerFaces = outerFaces.size() > readerCount
                ? new ArrayList<>(outerFaces.subList(readerCount, readerCount + writerCount))
                : new ArrayList<>();

        // Place redstone readers
        for (Pair<BlockPos, Direction> face : readerFaces) {
            PartHelpers.addPart(level, face.getLeft(), face.getRight(),
                    PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
        }

        if (readerFaces.isEmpty() || writerFaces.isEmpty()) {
            return;
        }

        // Place a variable store for intermediate operator variables.
        // Position it outside the cable grid (one column to the right).
        BlockPos varStorePos = startPos.offset(CABLE_GRID_X + 1, 0, 0);
        level.setBlock(varStorePos, RegistryEntries.BLOCK_VARIABLE_STORE.get().defaultBlockState(), 2);
        BlockEntityVariablestore varStore = (BlockEntityVariablestore) level.getBlockEntity(varStorePos);

        // Place writers with context blocks and logic connections
        int usedOperators = 0;
        int varStoreSlot = 0;
        for (int i = 0; i < writerFaces.size(); i++) {
            Pair<BlockPos, Direction> writerFace = writerFaces.get(i);
            BlockPos cablePos = writerFace.getLeft();
            Direction dir = writerFace.getRight();

            // Place a redstone lamp as context block for the writer.
            // The lamp is placed adjacent to the cable in the writer direction.
            BlockPos lampPos = cablePos.relative(dir);
            if (level.isEmptyBlock(lampPos)) {
                level.setBlock(lampPos, Blocks.REDSTONE_LAMP.defaultBlockState(), 2);
            }

            // Get the reader to connect to this writer (round-robin)
            Pair<BlockPos, Direction> readerFace = readerFaces.get(i % readerFaces.size());
            PartPos readerPartPos = PartPos.of(level, readerFace.getLeft(), readerFace.getRight());
            PartHelpers.PartStateHolder<?, ?> readerHolder = PartHelpers.getPart(readerPartPos);
            if (readerHolder == null) {
                continue;
            }

            // Choose the connection configuration based on remaining operator budget
            int remainingOps = maxOperators - usedOperators;
            int connConfig = chooseConnectionConfig(random, remainingOps);

            // Place writer part
            PartHelpers.addPart(level, cablePos, dir, PartTypes.REDSTONE_WRITER,
                    new ItemStack(PartTypes.REDSTONE_WRITER.getItem()));

            PartPos writerPartPos = PartPos.of(level, cablePos, dir);

            // Connect reader to writer with the selected configuration
            if (varStore != null && varStoreSlot < BlockEntityVariablestore.INVENTORY_SIZE - 4) {
                int[] result = connectWithConfig(level, connConfig, readerHolder, writerPartPos, varStore, varStoreSlot);
                usedOperators += result[0];
                varStoreSlot += result[1];
            } else {
                // Fallback: direct boolean connection when variable store is full
                ItemStack var = GameTestHelpersIntegratedDynamics.createVariableFromReader(level,
                        Aspects.Read.Redstone.BOOLEAN_CLOCK, readerHolder.getState());
                GameTestHelpersIntegratedDynamics.placeVariableInWriter(level, writerPartPos,
                        Aspects.Write.Redstone.BOOLEAN, var);
            }
        }
    }

    /**
     * Connect a reader to a writer using the given configuration.
     * Returns [operatorsUsed, varStoreSlotsUsed].
     *
     * Connection configurations:
     *  0 - BOOLEAN_CLOCK → BOOLEAN (direct)
     *  1 - INTEGER_VALUE → INTEGER (direct)
     *  2 - BOOLEAN_CLOCK → NOT → BOOLEAN  (1 op, 1 slot)
     *  3 - INTEGER_VALUE → INCREMENT → INTEGER  (1 op, 1 slot)
     *  4 - INTEGER_VALUE → ADD(5) → INTEGER  (1 op, 2 slots)
     *  5 - INTEGER_VALUE → MODULUS(15) → INTEGER  (1 op, 2 slots)
     *  6 - BOOLEAN_CLOCK → CHOICE(10, 5) → INTEGER  (1 op, 3 slots)
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static int[] connectWithConfig(net.minecraft.server.level.ServerLevel level, int config,
                                           PartHelpers.PartStateHolder<?, ?> readerHolder,
                                           PartPos writerPartPos,
                                           BlockEntityVariablestore varStore, int startSlot) {
        switch (config) {
            case 0: {
                // BOOLEAN_CLOCK → BOOLEAN (direct, no operators needed)
                ItemStack var = GameTestHelpersIntegratedDynamics.createVariableFromReader(level,
                        Aspects.Read.Redstone.BOOLEAN_CLOCK, readerHolder.getState());
                GameTestHelpersIntegratedDynamics.placeVariableInWriter(level, writerPartPos,
                        Aspects.Write.Redstone.BOOLEAN, var);
                return new int[]{0, 0};
            }
            case 1: {
                // INTEGER_VALUE → INTEGER (direct, no operators needed)
                ItemStack var = GameTestHelpersIntegratedDynamics.createVariableFromReader(level,
                        Aspects.Read.Redstone.INTEGER_VALUE, readerHolder.getState());
                GameTestHelpersIntegratedDynamics.placeVariableInWriter(level, writerPartPos,
                        Aspects.Write.Redstone.INTEGER, var);
                return new int[]{0, 0};
            }
            case 2: {
                // BOOLEAN_CLOCK → NOT → BOOLEAN
                ItemStack varClock = GameTestHelpersIntegratedDynamics.createVariableFromReader(level,
                        Aspects.Read.Redstone.BOOLEAN_CLOCK, readerHolder.getState());
                varStore.getInventory().setItem(startSlot, varClock);
                ItemStack varNot = GameTestHelpersIntegratedDynamics.createVariableForOperator(level,
                        Operators.LOGICAL_NOT, new int[]{
                                GameTestHelpersIntegratedDynamics.getVariableFacade(level, varClock).getId()
                        });
                GameTestHelpersIntegratedDynamics.placeVariableInWriter(level, writerPartPos,
                        Aspects.Write.Redstone.BOOLEAN, varNot);
                return new int[]{1, 1};
            }
            case 3: {
                // INTEGER_VALUE → INCREMENT → INTEGER
                ItemStack varInt = GameTestHelpersIntegratedDynamics.createVariableFromReader(level,
                        Aspects.Read.Redstone.INTEGER_VALUE, readerHolder.getState());
                varStore.getInventory().setItem(startSlot, varInt);
                ItemStack varInc = GameTestHelpersIntegratedDynamics.createVariableForOperator(level,
                        Operators.ARITHMETIC_INCREMENT, new int[]{
                                GameTestHelpersIntegratedDynamics.getVariableFacade(level, varInt).getId()
                        });
                GameTestHelpersIntegratedDynamics.placeVariableInWriter(level, writerPartPos,
                        Aspects.Write.Redstone.INTEGER, varInc);
                return new int[]{1, 1};
            }
            case 4: {
                // INTEGER_VALUE → ADD(5) → INTEGER
                ItemStack varInt = GameTestHelpersIntegratedDynamics.createVariableFromReader(level,
                        Aspects.Read.Redstone.INTEGER_VALUE, readerHolder.getState());
                ItemStack varConst = GameTestHelpersIntegratedDynamics.createVariableForValue(level,
                        ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(5));
                varStore.getInventory().setItem(startSlot, varInt);
                varStore.getInventory().setItem(startSlot + 1, varConst);
                ItemStack varAdd = GameTestHelpersIntegratedDynamics.createVariableForOperator(level,
                        Operators.ARITHMETIC_ADDITION, new int[]{
                                GameTestHelpersIntegratedDynamics.getVariableFacade(level, varInt).getId(),
                                GameTestHelpersIntegratedDynamics.getVariableFacade(level, varConst).getId()
                        });
                GameTestHelpersIntegratedDynamics.placeVariableInWriter(level, writerPartPos,
                        Aspects.Write.Redstone.INTEGER, varAdd);
                return new int[]{1, 2};
            }
            case 5: {
                // INTEGER_VALUE → MODULUS(15) → INTEGER (clamps value to 0-14 range for redstone signal)
                ItemStack varInt = GameTestHelpersIntegratedDynamics.createVariableFromReader(level,
                        Aspects.Read.Redstone.INTEGER_VALUE, readerHolder.getState());
                ItemStack varConst = GameTestHelpersIntegratedDynamics.createVariableForValue(level,
                        ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(15));
                varStore.getInventory().setItem(startSlot, varInt);
                varStore.getInventory().setItem(startSlot + 1, varConst);
                ItemStack varMod = GameTestHelpersIntegratedDynamics.createVariableForOperator(level,
                        Operators.ARITHMETIC_MODULUS, new int[]{
                                GameTestHelpersIntegratedDynamics.getVariableFacade(level, varInt).getId(),
                                GameTestHelpersIntegratedDynamics.getVariableFacade(level, varConst).getId()
                        });
                GameTestHelpersIntegratedDynamics.placeVariableInWriter(level, writerPartPos,
                        Aspects.Write.Redstone.INTEGER, varMod);
                return new int[]{1, 2};
            }
            case 6: {
                // BOOLEAN_CLOCK → CHOICE(10, 5) → INTEGER
                // Emulates the redstoneioclock_choice performance test pattern
                ItemStack varClock = GameTestHelpersIntegratedDynamics.createVariableFromReader(level,
                        Aspects.Read.Redstone.BOOLEAN_CLOCK, readerHolder.getState());
                ItemStack varIfTrue = GameTestHelpersIntegratedDynamics.createVariableForValue(level,
                        ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(10));
                ItemStack varIfFalse = GameTestHelpersIntegratedDynamics.createVariableForValue(level,
                        ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(5));
                varStore.getInventory().setItem(startSlot, varClock);
                varStore.getInventory().setItem(startSlot + 1, varIfTrue);
                varStore.getInventory().setItem(startSlot + 2, varIfFalse);
                ItemStack varChoice = GameTestHelpersIntegratedDynamics.createVariableForOperator(level,
                        Operators.GENERAL_CHOICE, new int[]{
                                GameTestHelpersIntegratedDynamics.getVariableFacade(level, varClock).getId(),
                                GameTestHelpersIntegratedDynamics.getVariableFacade(level, varIfTrue).getId(),
                                GameTestHelpersIntegratedDynamics.getVariableFacade(level, varIfFalse).getId()
                        });
                GameTestHelpersIntegratedDynamics.placeVariableInWriter(level, writerPartPos,
                        Aspects.Write.Redstone.INTEGER, varChoice);
                return new int[]{1, 3};
            }
            default: {
                // Fallback: direct boolean connection
                ItemStack var = GameTestHelpersIntegratedDynamics.createVariableFromReader(level,
                        Aspects.Read.Redstone.BOOLEAN_CLOCK, readerHolder.getState());
                GameTestHelpersIntegratedDynamics.placeVariableInWriter(level, writerPartPos,
                        Aspects.Write.Redstone.BOOLEAN, var);
                return new int[]{0, 0};
            }
        }
    }

    /**
     * Select a connection configuration based on the remaining operator budget.
     * Configs 0-1 use 0 operators, configs 2-6 use 1 operator each.
     */
    private static int chooseConnectionConfig(Random random, int remainingOps) {
        if (remainingOps <= 0) {
            // Only direct connections when budget is exhausted
            return random.nextInt(2);
        }
        return random.nextInt(7);
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
        template.fillFromWorld(level, captureFrom, size, false, null);

        CompoundTag tag = template.save(new CompoundTag());
        Path crashDir = Paths.get(CRASHES_DIR);
        Files.createDirectories(crashDir);
        NbtIo.writeCompressed(tag, crashDir.resolve(name + ".nbt"));
    }
}
