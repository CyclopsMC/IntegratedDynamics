package org.cyclops.integrateddynamics.gametest.fuzzing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeCategory;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.blockentity.BlockEntityDryingBasin;
import org.cyclops.integrateddynamics.blockentity.BlockEntityVariablestore;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics;

import javax.annotation.Nullable;
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
    private final List<BlockEntityVariablestore> varStores;
    private final ServerLevel level;

    // Track variable store and slot for current variable
    private int currentVarStoreIndex = 0;
    private int varStoreSlot = 0;

    // Track operator inputs that need reader sources
    private record OperatorInputNeeded(IOperator operator, int inputIndex, IValueType<?> expectedType) {}

    // Track variable IDs assigned to each operator's inputs
    private static class OperatorVariableAssignment {
        final IOperator operator;
        final int[] variableIds;

        OperatorVariableAssignment(IOperator operator, int numInputs) {
            this.operator = operator;
            this.variableIds = new int[numInputs];
        }
    }

    public NetworkFuzzer(Random random, int maxOperatorDepth, List<BlockPos> cables,
                         BlockEntityVariablestore varStore, ServerLevel level, BlockPos startPos) {
        this.random = random;
        this.maxOperatorDepth = maxOperatorDepth;
        this.cables = cables;
        this.varStores = new ArrayList<>();
        this.varStores.add(varStore);
        this.level = level;
    }

    /**
     * Get the current variable store.
     */
    private BlockEntityVariablestore getCurrentVarStore() {
        return varStores.get(currentVarStoreIndex);
    }

    /**
     * Advance to the next variable store slot, creating a new store on top if needed.
     *
     * @throws NetworkFuzzerException if creating a new store fails
     */
    private void advanceVarStoreSlot() throws NetworkFuzzerException {
        varStoreSlot++;

        if (varStoreSlot >= BlockEntityVariablestore.INVENTORY_SIZE) {
            // Current store is full, create a new one on top
            BlockEntityVariablestore currentStore = getCurrentVarStore();
            BlockPos currentStorePos = currentStore.getBlockPos();

            // Place new variable store on top of the current one
            BlockPos newStorePos = currentStorePos.above();
            if (!level.isEmptyBlock(newStorePos)) {
                throw new NetworkFuzzerException("Cannot place new variable store at " + newStorePos + " - block already exists");
            }

            level.setBlock(newStorePos, RegistryEntries.BLOCK_VARIABLE_STORE.get().defaultBlockState(), 2);

            // Get the new variable store block entity
            if (!(level.getBlockEntity(newStorePos) instanceof BlockEntityVariablestore newStore)) {
                throw new NetworkFuzzerException("Failed to get new variable store block entity at " + newStorePos);
            }

            // Add it to our list and update indices
            varStores.add(newStore);
            currentVarStoreIndex++;
            varStoreSlot = 0;
        }
    }

    /**
     * Generate a fuzzed network with three phases:
     * 1. Select a random writer part and aspect
     * 2. Build an operator chain that produces the required input type
     * 3. Select random readers to provide inputs to the operator chain
     *
     * @throws NetworkFuzzerException if an error occurs during network generation
     * @return If the generation succeeded (false if it failed due to lack of space or parts, true otherwise)
     */
    public boolean generate() throws NetworkFuzzerException {
        // Phase 1: Select writer
        Pair<IPartTypeWriter<?, ?>, IAspectWrite<?, ?>> writerAspect = selectRandomWriter();

        IPartTypeWriter<?, ?> writerType = writerAspect.getLeft();
        IAspectWrite<?, ?> writeAspect = writerAspect.getRight();
        IValueType<?> writerInputType = writeAspect.getValueType();

        // Place writer part
        Pair<BlockPos, Direction> writerPos = selectRandomOuterFace();
        if (writerPos == null) {
            return false; // No space to place writer
        }

        PartHelpers.addPart(level, writerPos.getLeft(), writerPos.getRight(),
                writerType, new ItemStack(writerType.getItem()));

        // Set up context blocks for the writer
        setupWriterContextBlocks(writerType, writerPos.getLeft(), writerPos.getRight());

        // Phase 2: Build operator chain
        List<IOperator> operatorChain = buildOperatorChain(writerInputType);

        // Phase 3: Select readers for unmapped inputs
        List<OperatorInputNeeded> inputsNeeded = determineInputsNeeded(operatorChain);
        Map<IOperator, OperatorVariableAssignment> operatorAssignments = new HashMap<>();
        for (IOperator op : operatorChain) {
            operatorAssignments.put(op, new OperatorVariableAssignment(op, op.getRequiredInputLength()));
        }
        fulfillInputs(inputsNeeded, operatorChain, operatorAssignments, writerPos.getLeft(), writerPos.getRight(), writeAspect);

        return true;
    }

    /**
     * Select a random writer part type and a random aspect from that part.
     */
    private Pair<IPartTypeWriter<?, ?>, IAspectWrite<?, ?>> selectRandomWriter() throws NetworkFuzzerException {
        List<IPartTypeWriter<?, ?>> writers = new ArrayList<>();

        // Collect all registered writer parts by checking if they implement IPartTypeWriter
        for (Object partType : PartTypes.REGISTRY.getPartTypes()) {
            if (partType instanceof IPartTypeWriter<?, ?> writer && !writer.getWriteAspects().isEmpty()) {
                writers.add(writer);
            }
        }

        if (writers.isEmpty()) {
            throw new NetworkFuzzerException("No writer parts are available");
        }

        IPartTypeWriter<?, ?> writerType = writers.get(random.nextInt(writers.size()));
        List<?> writeAspects = writerType.getWriteAspects();

        IAspectWrite<?, ?> aspect = (IAspectWrite<?, ?>) writeAspects.get(random.nextInt(writeAspects.size()));
        return Pair.of(writerType, aspect);
    }

    /**
     * Select a random outer face on the cable grid.
     */
    @Nullable
    private Pair<BlockPos, Direction> selectRandomOuterFace() {
        Set<BlockPos> cableSet = new HashSet<>(cables);
        List<Pair<BlockPos, Direction>> outerFaces = new ArrayList<>();

        for (BlockPos cable : cables) {
            for (Direction dir : Direction.values()) {
                BlockPos adjacent = cable.relative(dir);
                if (!cableSet.contains(adjacent) && PartHelpers.getPart(PartPos.of(DimPos.of(level, cable), dir)) == null) {
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
     *
     * @throws NetworkFuzzerException if building the chain fails
     */
    private List<IOperator> buildOperatorChain(IValueType<?> requiredType) throws NetworkFuzzerException {
        List<IOperator> chain = new ArrayList<>();
        IValueType<?> currentType = requiredType;
        int depth = random.nextInt(maxOperatorDepth + 1);

        for (int i = 0; i < depth; i++) {
            // Find an operator that produces currentType
            IOperator op = findRandomOperatorProducing(currentType);
            if (op == null) {
                break;
            }
            chain.addFirst(op); // Add to front since we're building backwards

            // Get the first input type of this operator
            IValueType<?>[] inputTypes = op.getInputTypes();
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
    private IOperator findRandomOperatorProducing(IValueType<?> valueType) throws NetworkFuzzerException {
        List<IOperator> matching = new ArrayList<>(findOperatorsProducingType(valueType));

        if (matching.isEmpty()) {
            throw new NetworkFuzzerException("No operators are available");
        }

        return matching.get(random.nextInt(matching.size()));
    }

    /**
     * Find operators producing a specific value type.
     * This method uses Operators.REGISTRY.getOperatorsWithOutputType() to find all operators
     * that produce the given type.
     *
     * @throws NetworkFuzzerException if the registry query fails
     */
    private List<IOperator> findOperatorsProducingType(IValueType<?> valueType) throws NetworkFuzzerException {
        // Narrow down category types
        if (valueType.isCategory()) {
            IValueTypeCategory<?> valueTypeCategory = (IValueTypeCategory<?>) valueType;
            List<IValueType> valueTypes = ValueTypes.REGISTRY.getValueTypes().stream()
                    .filter(valueTypeCategory::correspondsTo)
                    .toList();
            valueType = valueTypes.get(random.nextInt(valueTypes.size()));
        }

        List<IOperator> result = new ArrayList<>();

        try {
            // Use the registry's built-in method to get operators with the specified output type
            IValueType<?> finalValueType = valueType;
            result.addAll(Operators.REGISTRY.getOperatorsWithOutputType(valueType)
                    // Only keep operators that require entity inputs at a 10% chance.
                    // Otherwise, the TARGETENTITY operator is overwhelmingly common since it is the only one that produces an entity type.
                    .stream().filter(operator -> random.nextInt(10) == 9 || finalValueType == ValueTypes.OBJECT_ENTITY || Arrays.stream(operator.getInputTypes()).noneMatch(vt -> vt == ValueTypes.OBJECT_ENTITY)).toList());
        } catch (Exception e) {
            // If the registry query fails, throw an exception
            throw new NetworkFuzzerException("Failed to find operators for type " + valueType, e);
        }

        return result;
    }

    /**
     * Determine which operator inputs still need to be satisfied.
     */
    private List<OperatorInputNeeded> determineInputsNeeded(List<IOperator> chain) {
        List<OperatorInputNeeded> result = new ArrayList<>();

        for (IOperator op : chain) {
            IValueType<?>[] inputTypes = op.getInputTypes();
            for (int i = 0; i < inputTypes.length; i++) {
                // For now, mark all inputs as needed
                // In a full implementation, we'd track which are already provided by previous operators
                result.add(new OperatorInputNeeded(op, i, inputTypes[i]));
            }
        }

        return result;
    }

    /**
     * Generate a random constant value for the given value type.
     * Supports all value types except CATEGORY types.
     */
    @Nullable
    private IValue generateRandomConstantValue(IValueType<?> valueType) {
        // Skip category types
        if (valueType.isCategory()) {
            return null;
        }

        // Raw value types
        if (valueType == ValueTypes.BOOLEAN) {
            return ValueTypeBoolean.ValueBoolean.of(random.nextBoolean());
        } else if (valueType == ValueTypes.INTEGER) {
            return ValueTypeInteger.ValueInteger.of(random.nextInt(100) - 50);
        } else if (valueType == ValueTypes.DOUBLE) {
            return ValueTypeDouble.ValueDouble.of(random.nextDouble() * 100 - 50);
        } else if (valueType == ValueTypes.LONG) {
            return ValueTypeLong.ValueLong.of(random.nextLong());
        } else if (valueType == ValueTypes.STRING) {
            String[] examples = {"test", "hello", "world", "value", "data", "abc", "xyz"};
            return ValueTypeString.ValueString.of(examples[random.nextInt(examples.length)]);
        } else if (valueType == ValueTypes.LIST) {
            // Return empty list with CATEGORY_ANY type
            return ValueTypeList.ValueList.ofList(ValueTypes.CATEGORY_ANY, new ArrayList<>());
        } else if (valueType == ValueTypes.OPERATOR) {
            // Return a random operator wrapped as a value
            try {
                List<IOperator> operators = new ArrayList<>(Operators.REGISTRY.getOperators());
                if (!operators.isEmpty()) {
                    IOperator op = operators.get(random.nextInt(operators.size()));
                    return ValueTypeOperator.ValueOperator.of(op);
                }
            } catch (Exception e) {
                // If we can't get operators, return null
            }
            return null;
        } else if (valueType == ValueTypes.NBT) {
            CompoundTag tag = new CompoundTag();
            tag.putString("fuzzer", "generated");
            return ValueTypeNbt.ValueNbt.of(tag);
        }
        // Object types
        else if (valueType == ValueTypes.OBJECT_BLOCK) {
            // Return a random block
            var blocks = net.minecraft.core.registries.BuiltInRegistries.BLOCK.stream()
                    .filter(block -> net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block)
                            .getNamespace().equals("minecraft"))
                    .toList();
            if (!blocks.isEmpty()) {
                return ValueObjectTypeBlock.ValueBlock.of(blocks.get(random.nextInt(blocks.size())).defaultBlockState());
            }
            return null;
        } else if (valueType == ValueTypes.OBJECT_ITEMSTACK) {
            return ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(getRandomItem(), random.nextInt(64) + 1));
        } else if (valueType == ValueTypes.OBJECT_ENTITY) {
            // Return null for entity (can't easily create without spawning)
            return null;
        } else if (valueType == ValueTypes.OBJECT_FLUIDSTACK) {
            // Return a water fluid stack
            net.neoforged.neoforge.fluids.FluidStack fluidStack = new net.neoforged.neoforge.fluids.FluidStack(
                    net.minecraft.world.level.material.Fluids.WATER, 1000);
            return ValueObjectTypeFluidStack.ValueFluidStack.of(fluidStack);
        } else if (valueType == ValueTypes.OBJECT_INGREDIENTS) {
            // Return null for ingredients (complex to create)
            return null;
        } else if (valueType == ValueTypes.OBJECT_RECIPE) {
            // Return null for recipe (complex to create)
            return null;
        }

        return null;
    }

    /**
     * Fulfill the operator inputs by selecting random readers or constant values.
     * Tracks which variable IDs are assigned to each operator's inputs.
     * 50% chance to use a constant value instead of placing a reader.
     *
     * @throws NetworkFuzzerException if fulfilling inputs fails
     */
    private void fulfillInputs(List<OperatorInputNeeded> inputsNeeded, List<IOperator> operatorChain,
                               Map<IOperator, OperatorVariableAssignment> operatorAssignments,
                               BlockPos writerPos, Direction writerDir,
                               IAspectWrite<?, ?> writeAspect) throws NetworkFuzzerException {
        for (OperatorInputNeeded input : inputsNeeded) {
            // 50% chance to use a constant value instead of a reader
            if (random.nextBoolean()) {
                IValue constantValue = generateRandomConstantValue(input.expectedType);
                if (constantValue != null) {
                    // Create a variable from the constant value
                    ItemStack constantVar = GameTestHelpersIntegratedDynamics.createVariableForValue(level, input.expectedType, constantValue);

                    // Store the variable and track its ID
                    int variableId = constantVar.getCapability(Capabilities.VariableFacade.ITEM).getVariableFacade(ValueDeseralizationContext.of(level)).getId();
                    getCurrentVarStore().getInventory().setItem(varStoreSlot, constantVar);
                    advanceVarStoreSlot();

                    // Record this variable ID for the operator's input
                    OperatorVariableAssignment assignment = operatorAssignments.get(input.operator);
                    if (assignment != null && input.inputIndex < assignment.variableIds.length) {
                        assignment.variableIds[input.inputIndex] = variableId;
                    }
                    continue;
                }
            }

            // Place a reader (original behavior)
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

            // Store the variable and track its ID
            int variableId = readerVar.getCapability(Capabilities.VariableFacade.ITEM).getVariableFacade(ValueDeseralizationContext.of(level)).getId();
            getCurrentVarStore().getInventory().setItem(varStoreSlot, readerVar);
            advanceVarStoreSlot();

            // Record this variable ID for the operator's input
            OperatorVariableAssignment assignment = operatorAssignments.get(input.operator);
            if (assignment != null && input.inputIndex < assignment.variableIds.length) {
                assignment.variableIds[input.inputIndex] = variableId;
            }
        }

        // Finally, create the variable for the writer based on operator chain or reader
        PartPos writerPartPos = PartPos.of(level, writerPos, writerDir);
        PartHelpers.PartStateHolder<?, ?> writerHolder = PartHelpers.getPart(writerPartPos);
        if (writerHolder != null) {
            ItemStack writerVar;

            if (!operatorChain.isEmpty()) {
                // Create variable from the last operator in the chain
                IOperator lastOperator = operatorChain.getLast();
                OperatorVariableAssignment assignment = operatorAssignments.get(lastOperator);
                writerVar = GameTestHelpersIntegratedDynamics.createVariableForOperator(level, lastOperator, assignment.variableIds);
            } else {
                // No operators, create variable directly from a random constant
                IValue writerValue = generateRandomConstantValue(writeAspect.getValueType());
                if (writerValue != null) {
                    // Create a variable from the constant value
                    writerVar = GameTestHelpersIntegratedDynamics.createVariableForValue(level, writeAspect.getValueType(), writerValue);

                    // Store the variable
                    getCurrentVarStore().getInventory().setItem(varStoreSlot, writerVar);
                    advanceVarStoreSlot();
                } else {
                    return;
                }
            }

            GameTestHelpersIntegratedDynamics.placeVariableInWriter(level, writerPartPos,
                    writeAspect, writerVar);
        }
    }

    /**
     * Select a random reader part type and aspect that produces the required value type.
     */
    @Nullable
    private Pair<IPartTypeReader<?, ?>, IAspectRead<?, ?>> selectRandomReaderWithType(IValueType<?> valueType) {
        List<Pair<IPartTypeReader<?, ?>, IAspectRead<?, ?>>> validCombos = new ArrayList<>();

        // Collect all registered reader parts by checking if they implement IPartTypeReader
        for (Object partType : PartTypes.REGISTRY.getPartTypes()) {
            if (partType instanceof IPartTypeReader<?, ?> reader) {
                List<?> aspects = reader.getReadAspects();
                for (Object aspect : aspects) {
                    if (aspect instanceof IAspectRead<?, ?> readAspect) {
                        if (readAspect.getValueType() == valueType) {
                            validCombos.add(Pair.of(reader, readAspect));
                        }
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
     * This method intelligently places blocks and entities based on reader part type names.
     *
     * @throws NetworkFuzzerException if setting up context blocks fails
     */
    private void setupReaderContextBlocks(IPartTypeReader<?, ?> readerType, BlockPos cablePos, Direction readerDir) throws NetworkFuzzerException {
        BlockPos contextPos = cablePos.relative(readerDir);

        if (!level.isEmptyBlock(contextPos)) {
            return; // Don't overwrite existing blocks
        }

        String readerName = readerType.getClass().getSimpleName().toLowerCase();

        // Place context blocks based on reader type
        if (readerName.contains("inventory")) {
            placeChestWithItems(contextPos);
        } else if (readerName.contains("fluid")) {
            placeFluidSource(contextPos);
        } else if (readerName.contains("block")) {
            placeRandomBlock(contextPos);
        } else if (readerName.contains("audio")) {
            level.setBlock(contextPos, Blocks.NOTE_BLOCK.defaultBlockState(), 2);
        } else if (readerName.contains("redstone")) {
            placeRedstoneSource(contextPos);
        } else if (readerName.contains("machine")) {
            placeFurnaceWithItems(contextPos);
        } else if (readerName.contains("entity")) {
            spawnRandomEntity(contextPos);
        }
        // Network, World, and Extradimensional readers don't need special context blocks
    }

    /**
     * Set up context blocks for different writer types.
     * This method intelligently places blocks based on writer part type names.
     * Currently supports redstone writers.
     *
     * @throws NetworkFuzzerException if setting up context blocks fails
     */
    private void setupWriterContextBlocks(IPartTypeWriter<?, ?> writerType, BlockPos cablePos, Direction writerDir) throws NetworkFuzzerException {
        BlockPos contextPos = cablePos.relative(writerDir);

        if (!level.isEmptyBlock(contextPos)) {
            return; // Don't overwrite existing blocks
        }

        String writerName = writerType.getClass().getSimpleName().toLowerCase();

        // Place context blocks based on writer type
        if (writerName.contains("redstone")) {
            placeRedstoneWriterTarget(contextPos);
        }
        // Other writer types don't need special context blocks for now
    }

    /**
     * Place a target block for redstone writers (redstone dust or redstone lamp).
     */
    private void placeRedstoneWriterTarget(BlockPos pos) {
        // Randomly choose between redstone dust and redstone lamp
        if (random.nextBoolean()) {
            // Place redstone dust (needs a solid block beneath it)
            BlockPos basePos = pos.below();
            if (level.isEmptyBlock(basePos)) {
                level.setBlock(basePos, Blocks.STONE.defaultBlockState(), 2);
            }
            level.setBlock(pos, Blocks.REDSTONE_WIRE.defaultBlockState(), 2);
        } else {
            // Place redstone lamp
            level.setBlock(pos, Blocks.REDSTONE_LAMP.defaultBlockState(), 2);
        }
    }

    /**
     * Place a chest with random items inside.
     */
    private void placeChestWithItems(BlockPos pos) {
        level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 2);

        // Add some random items to the chest
        if (level.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chestEntity) {
            int itemCount = random.nextInt(3) + 1; // 1-3 random items
            for (int i = 0; i < itemCount && i < chestEntity.getContainerSize(); i++) {
                net.minecraft.world.item.ItemStack itemStack = new ItemStack(
                        getRandomItem(),
                        random.nextInt(64) + 1
                );
                chestEntity.setItem(i, itemStack);
            }
        }
    }

    /**
     * Place a furnace block with random valid items in the input slot (0) and fuel slot (1).
     * Each slot has a 1/10 chance of being left empty.
     */
    private void placeFurnaceWithItems(BlockPos pos) {
        level.setBlock(pos, Blocks.FURNACE.defaultBlockState(), 2);

        if (level.getBlockEntity(pos) instanceof FurnaceBlockEntity furnace) {
            // Slot 0: input item (smeltable) — 1/10 chance to leave empty
            if (random.nextInt(10) != 0) {
                Item inputItem = getRandomSmeltableItem();
                furnace.setItem(0, new ItemStack(inputItem, random.nextInt(64) + 1));
            }

            // Slot 1: fuel item — 1/10 chance to leave empty
            if (random.nextInt(10) != 0) {
                Item fuelItem = getRandomFuelItem();
                furnace.setItem(1, new ItemStack(fuelItem, random.nextInt(64) + 1));
            }
        }
    }

    /**
     * Get a random vanilla smeltable item (valid input for a furnace).
     */
    private net.minecraft.world.item.Item getRandomSmeltableItem() {
        // A curated list of vanilla items that can be smelted
        Item[] smeltables = {
                Items.RAW_IRON,
                Items.RAW_GOLD,
                Items.RAW_COPPER,
                Items.IRON_ORE,
                Items.GOLD_ORE,
                Items.COPPER_ORE,
                Items.SAND,
                Items.COBBLESTONE,
                Items.NETHERRACK,
                Items.CLAY_BALL,
                Items.BEEF,
                Items.PORKCHOP,
                Items.POTATO,
                Items.WET_SPONGE,
                Items.ANCIENT_DEBRIS,
        };
        return smeltables[random.nextInt(smeltables.length)];
    }

    /**
     * Get a random vanilla fuel item (valid fuel for a furnace).
     * Uses getBurnTime to confirm validity, falling back to COAL if no fuel items are found.
     */
    private net.minecraft.world.item.Item getRandomFuelItem() {
        // Filter all minecraft items to those that are valid furnace fuel
        List<net.minecraft.world.item.Item> fuelItems = BuiltInRegistries.ITEM.stream()
                .filter(item -> BuiltInRegistries.ITEM.getKey(item)
                        .getNamespace().equals("minecraft"))
                .filter(item -> new ItemStack(item).getBurnTime(RecipeType.SMELTING) > 0)
                .toList();

        if (fuelItems.isEmpty()) {
            return net.minecraft.world.item.Items.COAL;
        }
        return fuelItems.get(random.nextInt(fuelItems.size()));
    }

    /**
     * Get a random item for placing in containers.
     * Only selects items from the minecraft namespace.
     */
    private net.minecraft.world.item.Item getRandomItem() {
        // Filter to only minecraft namespace items and get a random one
        var minecraftItems = BuiltInRegistries.ITEM.stream()
                .filter(item -> BuiltInRegistries.ITEM.getKey(item)
                        .getNamespace().equals("minecraft"))
                .toList();

        if (minecraftItems.isEmpty()) {
            return net.minecraft.world.item.Items.STONE;
        }

        return minecraftItems.get(random.nextInt(minecraftItems.size()));
    }

    /**
     * Place a fluid source block (cauldron, fluid block, or drying basin with fluid).
     * 50% chance to place a drying basin with a random fluid type and amount.
     */
    private void placeFluidSource(BlockPos pos) {
        if (random.nextBoolean()) {
            // 50% chance: place a drying basin with a random fluid and amount
            placeDryingBasinWithFluid(pos);
        } else {
            // 50% chance: place cauldron or water block (original behavior)
            if (random.nextBoolean()) {
                level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 2);
            } else {
                level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
            }
        }
    }

    /**
     * Place a drying basin block with a random fluid type and a random amount (1 to BUCKET_VOLUME).
     */
    private void placeDryingBasinWithFluid(BlockPos pos) {
        level.setBlock(pos, RegistryEntries.BLOCK_DRYING_BASIN.get().defaultBlockState(), 2);

        if (level.getBlockEntity(pos) instanceof BlockEntityDryingBasin dryingBasin) {
            // Pick a random non-empty fluid from the minecraft namespace
            List<Fluid> fluids = BuiltInRegistries.FLUID.stream()
                    .filter(fluid -> !fluid.isSame(net.minecraft.world.level.material.Fluids.EMPTY)
                            && BuiltInRegistries.FLUID.getKey(fluid)
                                .getNamespace().equals("minecraft"))
                    .toList();

            if (!fluids.isEmpty()) {
                Fluid fluid = fluids.get(random.nextInt(fluids.size()));
                int amount = random.nextInt(FluidHelpers.BUCKET_VOLUME) + 1;
                dryingBasin.getTank().setFluid(new FluidStack(fluid, amount));
            }
        }
    }

    /**
     * Place a random solid block.
     * Only selects blocks from the minecraft namespace.
     */
    private void placeRandomBlock(BlockPos pos) {
        // Filter to only minecraft namespace blocks and get a random one
        var minecraftBlocks = BuiltInRegistries.BLOCK.stream()
                .filter(block -> BuiltInRegistries.BLOCK.getKey(block)
                        .getNamespace().equals("minecraft"))
                .toList();

        if (!minecraftBlocks.isEmpty()) {
            Block block = minecraftBlocks.get(random.nextInt(minecraftBlocks.size()));
            level.setBlock(pos, block.defaultBlockState(), 2);
        }
    }

    /**
     * Place a redstone source block (torch, wire, repeater, etc).
     */
    private void placeRedstoneSource(BlockPos pos) {
        // Randomly choose between different redstone sources
        int choice = random.nextInt(4);
        switch (choice) {
            case 0:
                // Redstone torch
                level.setBlock(pos, Blocks.REDSTONE_TORCH.defaultBlockState(), 2);
                break;
            case 1:
                // Redstone wire (place on top of a block)
                BlockPos basePos = pos.below();
                if (level.isEmptyBlock(basePos)) {
                    level.setBlock(basePos, Blocks.STONE.defaultBlockState(), 2);
                }
                level.setBlock(pos, Blocks.REDSTONE_WIRE.defaultBlockState(), 2);
                break;
            case 2:
                // Redstone repeater
                level.setBlock(pos, Blocks.REPEATER.defaultBlockState(), 2);
                break;
            case 3:
                // Redstone comparator
                level.setBlock(pos, Blocks.COMPARATOR.defaultBlockState(), 2);
                break;
        }
    }

    /**
     * Spawn a random entity in front of the reader.
     * Only selects entity types from the minecraft namespace.
     *
     * @throws NetworkFuzzerException if spawning an entity fails
     */
    private void spawnRandomEntity(BlockPos pos) throws NetworkFuzzerException {
        // Filter to only minecraft namespace entity types and get a random one
        var minecraftEntities = BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(entityType -> BuiltInRegistries.ENTITY_TYPE.getKey(entityType)
                        .getNamespace().equals("minecraft"))
                .toList();

        if (minecraftEntities.isEmpty()) {
            throw new NetworkFuzzerException("No entities are available to spawn");
        }

        EntityType<?> entityType = minecraftEntities.get(random.nextInt(minecraftEntities.size()));

        try {
            Entity entity = entityType.create(level);
            if (entity != null) {
                entity.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                level.addFreshEntity(entity);
            }
        } catch (Exception e) {
            // Some entity types might fail to spawn
            throw new NetworkFuzzerException("Failed to spawn entity of type " + entityType, e);
        }
    }
}
