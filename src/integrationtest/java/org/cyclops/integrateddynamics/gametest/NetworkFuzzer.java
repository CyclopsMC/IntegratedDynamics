package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.blockentity.BlockEntityVariablestore;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypes;

import java.util.*;

/**
 * Generates random fuzzed networks with diverse parts, aspects, and operators.
 * Orchestrates three phases: writer selection, operator chain building, and reader selection.
 *
 * @author rubensworks
 */
public class NetworkFuzzer {

    private final Random random;
    private final int maxOperatorDepth;
    private final List<BlockPos> cables;
    private final BlockEntityVariablestore varStore;
    private final ServerLevel level;

    // Track variable store capacity
    private int varStoreSlot = 0;

    // Track operator inputs that need reader sources
    private static class OperatorInputNeeded {
        final IOperator operator;
        final int inputIndex;
        @SuppressWarnings("rawtypes")
        final IValueType expectedType;

        @SuppressWarnings("rawtypes")
        OperatorInputNeeded(IOperator operator, int inputIndex, IValueType expectedType) {
            this.operator = operator;
            this.inputIndex = inputIndex;
            this.expectedType = expectedType;
        }
    }

    public NetworkFuzzer(Random random, int maxOperatorDepth, List<BlockPos> cables,
                         BlockEntityVariablestore varStore, ServerLevel level, BlockPos startPos) {
        this.random = random;
        this.maxOperatorDepth = maxOperatorDepth;
        this.cables = cables;
        this.varStore = varStore;
        this.level = level;
    }

    /**
     * Generate a fuzzed network with three phases:
     * 1. Select a random writer part and aspect
     * 2. Build an operator chain that produces the required input type
     * 3. Select random readers to provide inputs to the operator chain
     */
    public void generate() {
        try {
            // Phase 1: Select writer
            Pair<IPartTypeWriter<?, ?>, IAspectWrite<?, ?>> writerAspect = selectRandomWriter();
            if (writerAspect == null) {
                IntegratedDynamics.clog(Level.WARN, "[Fuzzing] No valid writers found");
                return;
            }

            IPartTypeWriter<?, ?> writerType = writerAspect.getLeft();
            IAspectWrite<?, ?> writeAspect = writerAspect.getRight();
            @SuppressWarnings("rawtypes")
            IValueType writerInputType = writeAspect.getValueType();

            // Place writer part
            Pair<BlockPos, Direction> writerPos = selectRandomOuterFace();
            if (writerPos == null) {
                IntegratedDynamics.clog(Level.WARN, "[Fuzzing] No valid writer position found");
                return;
            }

            PartHelpers.addPart(level, writerPos.getLeft(), writerPos.getRight(),
                    writerType, new ItemStack(writerType.getItem()));

            // Phase 2: Build operator chain
            List<IOperator> operatorChain = buildOperatorChain(writerInputType);

            // Phase 3: Select readers for unmapped inputs
            List<OperatorInputNeeded> inputsNeeded = determineInputsNeeded(operatorChain);
            fulfillInputs(inputsNeeded, writerPos.getLeft(), writerPos.getRight(), writeAspect);

        } catch (Exception e) {
            IntegratedDynamics.clog(Level.ERROR, "[Fuzzing] Error in NetworkFuzzer.generate(): " + e);
        }
    }

    /**
     * Select a random writer part type and a random aspect from that part.
     */
    private Pair<IPartTypeWriter<?, ?>, IAspectWrite<?, ?>> selectRandomWriter() {
        List<IPartTypeWriter<?, ?>> writers = new ArrayList<>();

        // Collect all registered writer parts by checking if they implement IPartTypeWriter
        for (Object partType : PartTypes.REGISTRY.getPartTypes()) {
            if (partType instanceof IPartTypeWriter<?, ?>) {
                writers.add((IPartTypeWriter<?, ?>) partType);
            }
        }

        if (writers.isEmpty()) {
            return null;
        }

        IPartTypeWriter<?, ?> writerType = writers.get(random.nextInt(writers.size()));
        @SuppressWarnings("rawtypes")
        List writeAspects = writerType.getWriteAspects();

        if (writeAspects.isEmpty()) {
            return null;
        }

        @SuppressWarnings("unchecked")
        IAspectWrite<?, ?> aspect = (IAspectWrite<?, ?>) writeAspects.get(random.nextInt(writeAspects.size()));
        return Pair.of(writerType, aspect);
    }

    /**
     * Select a random outer face on the cable grid.
     */
    private Pair<BlockPos, Direction> selectRandomOuterFace() {
        Set<BlockPos> cableSet = new HashSet<>(cables);
        List<Pair<BlockPos, Direction>> outerFaces = new ArrayList<>();

        for (BlockPos cable : cables) {
            for (Direction dir : Direction.values()) {
                BlockPos adjacent = cable.relative(dir);
                if (!cableSet.contains(adjacent)) {
                    outerFaces.add(Pair.of(cable, dir));
                }
            }
        }

        if (outerFaces.isEmpty()) {
            return null;
        }

        return outerFaces.get(random.nextInt(outerFaces.size()));
    }

    /**
     * Build an operator chain that produces the required value type.
     * Returns a list of operators in order from input to output.
     */
    @SuppressWarnings("rawtypes")
    private List<IOperator> buildOperatorChain(IValueType requiredType) {
        List<IOperator> chain = new ArrayList<>();
        IValueType currentType = requiredType;
        int depth = random.nextInt(maxOperatorDepth + 1);

        for (int i = 0; i < depth && varStoreSlot < BlockEntityVariablestore.INVENTORY_SIZE - 1; i++) {
            // Find an operator that produces currentType
            IOperator op = findRandomOperatorProducing(currentType);
            if (op == null) {
                break;
            }
            chain.addFirst(op); // Add to front since we're building backwards

            // Get the first input type of this operator
            IValueType[] inputTypes = op.getInputTypes();
            if (inputTypes.length > 0) {
                currentType = inputTypes[0];
            } else {
                break;
            }
        }

        return chain;
    }

    /**
     * Find a random operator that produces the given value type.
     */
    @SuppressWarnings("rawtypes")
    private IOperator findRandomOperatorProducing(IValueType valueType) {
        List<IOperator> matching = new ArrayList<>(findOperatorsProducingType(valueType));

        if (matching.isEmpty()) {
            return null;
        }

        return matching.get(random.nextInt(matching.size()));
    }

    /**
     * Find operators producing a specific value type.
     * This method uses Operators.REGISTRY.getOperatorsWithOutputType() to find all operators
     * that produce the given type.
     */
    @SuppressWarnings("rawtypes")
    private List<IOperator> findOperatorsProducingType(IValueType valueType) {
        List<IOperator> result = new ArrayList<>();

        try {
            // Use the registry's built-in method to get operators with the specified output type
            result.addAll(Operators.REGISTRY.getOperatorsWithOutputType(valueType));
        } catch (Exception e) {
            // If the registry query fails, just return empty list
            IntegratedDynamics.clog(Level.DEBUG, "[Fuzzing] Failed to find operators for type " + valueType + ": " + e.getMessage());
        }

        return result;
    }

    /**
     * Determine which operator inputs still need to be satisfied.
     */
    private List<OperatorInputNeeded> determineInputsNeeded(List<IOperator> chain) {
        List<OperatorInputNeeded> result = new ArrayList<>();

        for (IOperator op : chain) {
            @SuppressWarnings("rawtypes")
            IValueType[] inputTypes = op.getInputTypes();
            for (int i = 0; i < inputTypes.length; i++) {
                // For now, mark all inputs as needed
                // In a full implementation, we'd track which are already provided by previous operators
                result.add(new OperatorInputNeeded(op, i, inputTypes[i]));
            }
        }

        return result;
    }

    /**
     * Fulfill the operator inputs by selecting random readers.
     */
    @SuppressWarnings("unchecked")
    private void fulfillInputs(List<OperatorInputNeeded> inputsNeeded, BlockPos writerPos, Direction writerDir,
                               IAspectWrite<?, ?> writeAspect) {
        for (OperatorInputNeeded input : inputsNeeded) {
            if (varStoreSlot >= BlockEntityVariablestore.INVENTORY_SIZE - 1) {
                break;
            }

            Pair<IPartTypeReader<?, ?>, IAspectRead<?, ?>> readerAspect = selectRandomReaderWithType(input.expectedType);
            if (readerAspect == null) {
                continue;
            }

            // Place reader part
            Pair<BlockPos, Direction> readerPos = selectRandomOuterFace();
            if (readerPos == null) {
                continue;
            }

            IPartTypeReader<?, ?> readerType = readerAspect.getLeft();
            IAspectRead<?, ?> readAspect = readerAspect.getRight();

            PartHelpers.addPart(level, readerPos.getLeft(), readerPos.getRight(),
                    readerType, new ItemStack(readerType.getItem()));

            // Set up context blocks for the reader
            setupReaderContextBlocks(readerType, readerPos.getLeft(), readerPos.getRight());

            // Create variable from reader and store it
            PartPos readerPartPos = PartPos.of(level, readerPos.getLeft(), readerPos.getRight());
            PartHelpers.PartStateHolder<?, ?> readerHolder = PartHelpers.getPart(readerPartPos);
            if (readerHolder == null) {
                continue;
            }

            ItemStack readerVar = GameTestHelpersIntegratedDynamics.createVariableFromReader(level,
                    readAspect, readerHolder.getState());
            varStore.getInventory().setItem(varStoreSlot++, readerVar);
        }

        // Finally, create the operator variable(s) and place in the writer
        PartPos writerPartPos = PartPos.of(level, writerPos, writerDir);
        PartHelpers.PartStateHolder<?, ?> writerHolder = PartHelpers.getPart(writerPartPos);
        if (writerHolder != null) {
            ItemStack writerVar = GameTestHelpersIntegratedDynamics.createVariableFromReader(level,
                    writeAspect, writerHolder.getState());
            GameTestHelpersIntegratedDynamics.placeVariableInWriter(level, writerPartPos,
                    writeAspect, writerVar);
        }
    }

    /**
     * Select a random reader part type and aspect that produces the required value type.
     */
    @SuppressWarnings("rawtypes")
    private Pair<IPartTypeReader<?, ?>, IAspectRead<?, ?>> selectRandomReaderWithType(IValueType valueType) {
        List<Pair<IPartTypeReader<?, ?>, IAspectRead<?, ?>>> validCombos = new ArrayList<>();

        // Collect all registered reader parts by checking if they implement IPartTypeReader
        for (Object partType : PartTypes.REGISTRY.getPartTypes()) {
            if (partType instanceof IPartTypeReader<?, ?>) {
                IPartTypeReader<?, ?> reader = (IPartTypeReader<?, ?>) partType;
                @SuppressWarnings("unchecked")
                List<IAspectRead<?, ?>> aspects = (List<IAspectRead<?, ?>>) (List<?>) reader.getReadAspects();
                for (IAspectRead<?, ?> aspect : aspects) {
                    if (aspect.getValueType() == valueType) {
                        validCombos.add(Pair.of(reader, aspect));
                    }
                }
            }
        }

        if (validCombos.isEmpty()) {
            return null;
        }

        return validCombos.get(random.nextInt(validCombos.size()));
    }

    /**
     * Set up context blocks for different reader types.
     * This method intelligently places blocks based on reader part type names.
     */
    private void setupReaderContextBlocks(IPartTypeReader<?, ?> readerType, BlockPos cablePos, Direction readerDir) {
        BlockPos contextPos = cablePos.relative(readerDir);

        if (!level.isEmptyBlock(contextPos)) {
            return; // Don't overwrite existing blocks
        }

        String readerName = readerType.getClass().getSimpleName().toLowerCase();

        // Place context blocks based on reader type
        if (readerName.contains("inventory")) {
            level.setBlock(contextPos, Blocks.CHEST.defaultBlockState(), 2);
        } else if (readerName.contains("fluid")) {
            level.setBlock(contextPos, Blocks.CAULDRON.defaultBlockState(), 2);
        } else if (readerName.contains("block")) {
            level.setBlock(contextPos, Blocks.STONE.defaultBlockState(), 2);
        } else if (readerName.contains("audio")) {
            level.setBlock(contextPos, Blocks.NOTE_BLOCK.defaultBlockState(), 2);
        } else if (readerName.contains("redstone")) {
            level.setBlock(contextPos, Blocks.REDSTONE_LAMP.defaultBlockState(), 2);
        } else if (readerName.contains("machine")) {
            level.setBlock(contextPos, Blocks.FURNACE.defaultBlockState(), 2);
        } else {
            // Default for entity, network, world, extradimensional, etc: place stone
            level.setBlock(contextPos, Blocks.STONE.defaultBlockState(), 2);
        }
    }
}
