package org.cyclops.integrateddynamics.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.command.argument.ArgumentTypeEnum;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.blockentity.BlockEntityVariablestore;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypeRegistry;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics;
import org.cyclops.integrateddynamics.part.aspect.Aspects;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Command for generating networks with different presets.
 * @author rubensworks
 */
public class CommandGenerateNetwork implements Command<CommandSourceStack> {

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("generatenetwork")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));

        // Add the preset subcommand with optional size/radius argument
        builder.then(Commands.argument("preset", new ArgumentTypeEnum(NetworkPreset.class))
                .executes(new CommandGenerateNetworkExecutor(true, false))
                .then(Commands.argument("size", IntegerArgumentType.integer(1, 1000))
                        .executes(new CommandGenerateNetworkExecutor(true, true))));

        return builder;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        context.getSource().sendFailure(Component.literal("Please specify a preset: empty, idle, redstoneioclock, or clear")
                .withStyle(ChatFormatting.RED));
        return 0;
    }

    public enum NetworkPreset {
        EMPTY,
        IDLE,
        REDSTONEIOCLOCK,
        REDSTONEIOCLOCKVARIABLES,
        CLEAR
    }

    /**
     * Executor for the generatenetwork command.
     */
    public static class CommandGenerateNetworkExecutor implements Command<CommandSourceStack> {
        private final boolean hasPreset;
        private final boolean hasSize;

        public CommandGenerateNetworkExecutor(boolean hasPreset, boolean hasSize) {
            this.hasPreset = hasPreset;
            this.hasSize = hasSize;
        }

        @Override
        public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            if (!hasPreset) {
                context.getSource().sendFailure(Component.literal("Please specify a preset: empty, idle, redstoneioclock, or clear")
                        .withStyle(ChatFormatting.RED));
                return 0;
            }

            NetworkPreset preset = ArgumentTypeEnum.getValue(context, "preset", NetworkPreset.class);
            ServerLevel level = context.getSource().getLevel();
            BlockPos playerPos = BlockPos.containing(context.getSource().getPosition());
            int size = hasSize ? IntegerArgumentType.getInteger(context, "size") : getDefaultSize(preset);

            switch (preset) {
                case EMPTY:
                    context.getSource().sendSuccess(
                            () -> Component.literal("Generating network preset: empty (size: " + size + "x" + size + "x" + size + ")")
                                    .withStyle(ChatFormatting.GREEN),
                            true);
                    NetworkGenerationHelper.generateEmptyNetwork(level, playerPos.above(2), size);
                    break;
                case IDLE:
                    context.getSource().sendSuccess(
                            () -> Component.literal("Generating network preset: idle (size: " + size + "x" + size + "x" + size + ")")
                                    .withStyle(ChatFormatting.GREEN),
                            true);
                    NetworkGenerationHelper.generateIdleNetwork(level, playerPos.above(2), size);
                    break;
                case REDSTONEIOCLOCK:
                    context.getSource().sendSuccess(
                            () -> Component.literal("Generating network preset: redstoneioclock (size: " + size + "x" + size + "x" + size + ")")
                                    .withStyle(ChatFormatting.GREEN),
                            true);
                    NetworkGenerationHelper.generateRedstoneNetwork(level, playerPos.above(2), size);
                    break;
                case REDSTONEIOCLOCKVARIABLES:
                    context.getSource().sendSuccess(
                            () -> Component.literal("Generating network preset: redstoneioclockvariables (size: " + size + "x" + size + "x" + size + ")")
                                    .withStyle(ChatFormatting.GREEN),
                            true);
                    NetworkGenerationHelper.generateRedstoneNetworkVariables(level, playerPos.above(2), size);
                    break;
                case CLEAR:
                    context.getSource().sendSuccess(
                            () -> Component.literal("Clearing cables within radius: " + size)
                                    .withStyle(ChatFormatting.GREEN),
                            true);
                    NetworkGenerationHelper.clearCables(level, playerPos, size);
                    break;
            }

            return 1;
        }

        /**
         * Get the default size/radius for the given preset.
         */
        private int getDefaultSize(NetworkPreset preset) {
            return preset == NetworkPreset.CLEAR ? 50 : 25;
        }
    }

    /**
     * Helper class for network generation logic, shared between command and game tests.
     */
    public static class NetworkGenerationHelper {
        /**
         * Generate a size x size x size cube of only logic cables.
         */
        public static void generateEmptyNetwork(ServerLevel level, BlockPos startPos, int size) {
            List<BlockPos> placedPositions = new ArrayList<>();

            BlockCable.SKIP_NETWORK_INIT = true;
            try {
                for (int x = 0; x < size; x++) {
                    for (int y = 0; y < size; y++) {
                        for (int z = 0; z < size; z++) {
                            BlockPos pos = startPos.offset(x, y, z);
                            level.setBlock(pos, RegistryEntries.BLOCK_CABLE.value().defaultBlockState(), 2);
                            placedPositions.add(pos);
                        }
                    }
                }
            } finally {
                BlockCable.SKIP_NETWORK_INIT = false;
            }

            for (BlockPos pos : placedPositions) {
                CableHelpers.updateConnectionsNeighbours(level, pos, CableHelpers.ALL_SIDES);
            }

            NetworkHelpers.initNetwork(level, startPos, null);
        }

        /**
         * Generate a size x size x size cube of logic cables where all cables on the outer sides
         * contain random parts facing outwards.
         */
        public static void generateIdleNetwork(ServerLevel level, BlockPos startPos, int size) {
            generateEmptyNetwork(level, startPos, size);

            Random random = new Random();
            List<BlockPos> updatePositions = new ArrayList<>();

            addPartsToFace(level, startPos, size, 0, size - 1, size - 1, size - 1, 0, size - 1, Direction.UP, random, updatePositions);
            addPartsToFace(level, startPos, size, 0, size - 1, 0, 0, 0, size - 1, Direction.DOWN, random, updatePositions);
            addPartsToFace(level, startPos, size, 0, size - 1, 0, size - 1, 0, 0, Direction.NORTH, random, updatePositions);
            addPartsToFace(level, startPos, size, 0, size - 1, 0, size - 1, size - 1, size - 1, Direction.SOUTH, random, updatePositions);
            addPartsToFace(level, startPos, size, 0, 0, 0, size - 1, 0, size - 1, Direction.WEST, random, updatePositions);
            addPartsToFace(level, startPos, size, size - 1, size - 1, 0, size - 1, 0, size - 1, Direction.EAST, random, updatePositions);

            for (BlockPos pos : updatePositions) {
                level.updateNeighborsAt(pos, RegistryEntries.BLOCK_CABLE.value());
            }
        }

        /**
         * Place a single cable block at the given position.
         */
        public static void placeCable(ServerLevel level, BlockPos pos) {
            level.setBlock(pos, RegistryEntries.BLOCK_CABLE.value().defaultBlockState(), 2);
        }

        /**
         * Add a random part to the NORTH face of a cable at the given position.
         */
        public static void addPartToNorthFace(ServerLevel level, BlockPos pos) {
            List<IPartType> partTypes = new ArrayList<>(PartTypeRegistry.getInstance().getPartTypes());

            if (partTypes.isEmpty()) {
                return;
            }

            Random random = new Random();
            IPartType partType = partTypes.get(random.nextInt(partTypes.size()));
            ItemStack itemStack = new ItemStack(partType.getItem());
            PartHelpers.addPart(level, pos, Direction.NORTH, partType, itemStack);
        }

        /**
         * Clear all cable blocks within a radius of the given position.
         */
        public static void clearCables(ServerLevel level, BlockPos centerPos, int radius) {
            BlockCable.SKIP_NETWORK_INIT = true;

            try {
                for (int x = centerPos.getX() - radius; x <= centerPos.getX() + radius; x++) {
                    for (int y = centerPos.getY() - radius; y <= centerPos.getY() + radius; y++) {
                        for (int z = centerPos.getZ() - radius; z <= centerPos.getZ() + radius; z++) {
                            BlockPos pos = new BlockPos(x, y, z);
                            if (level.getBlockState(pos).getBlock() == RegistryEntries.BLOCK_CABLE.value()) {
                                level.destroyBlock(pos, false);
                            }
                        }
                    }
                }
            } finally {
                BlockCable.SKIP_NETWORK_INIT = false;
            }
        }

        private static void addPartsToFace(ServerLevel level, BlockPos startPos, int size,
                                          int minX, int maxX, int minY, int maxY, int minZ, int maxZ,
                                          Direction side, Random random, List<BlockPos> updatePositions) {
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        BlockPos pos = startPos.offset(x, y, z);
                        addRandomPartDeferred(level, pos, side, random, updatePositions);
                    }
                }
            }
        }

        private static void addRandomPartDeferred(ServerLevel level, BlockPos pos, Direction side, Random random, List<BlockPos> updatePositions) {
            List<IPartType> partTypes = new ArrayList<>(PartTypeRegistry.getInstance().getPartTypes());

            if (partTypes.isEmpty()) {
                return;
            }

            IPartType partType = partTypes.get(random.nextInt(partTypes.size()));
            ItemStack itemStack = new ItemStack(partType.getItem());
            PartHelpers.addPart(level, pos, side, partType, itemStack);
            updatePositions.add(pos);
        }

        /**
         * Generate a size x size x size cube of logic cables where all cables on the EAST side
         * contain redstone readers, and all cables on the WEST side contain redstone writers.
         * For each reader-writer pair at the same Y and Z coordinates, a variable is created
         * that reads the BOOLEAN_CLOCK aspect from the reader and connects it to the
         * BOOLEAN aspect of the writer at the opposite side.
         */
        public static void generateRedstoneNetwork(ServerLevel level, BlockPos startPos, int size) {
            generateEmptyNetwork(level, startPos, size);

            List<BlockPos> updatePositions = new ArrayList<>();

            // Add redstone readers to EAST side and redstone writers to WEST side
            // EAST side is at x = size - 1, WEST side is at x = 0
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    // EAST side: redstone reader
                    BlockPos eastPos = startPos.offset(size - 1, y, z);
                    PartHelpers.addPart(level, eastPos, Direction.EAST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
                    updatePositions.add(eastPos);

                    // WEST side: redstone writer
                    BlockPos westPos = startPos.offset(0, y, z);
                    PartHelpers.addPart(level, westPos, Direction.WEST, PartTypes.REDSTONE_WRITER, new ItemStack(PartTypes.REDSTONE_WRITER.getItem()));
                    updatePositions.add(westPos);
                }
            }

            // Update all positions and create variable connections
            for (BlockPos pos : updatePositions) {
                level.updateNeighborsAt(pos, RegistryEntries.BLOCK_CABLE.value());
            }

            // Create variables connecting readers to writers
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    BlockPos eastPos = startPos.offset(size - 1, y, z);
                    BlockPos westPos = startPos.offset(0, y, z);

                    // Create variable from reader's BOOLEAN_CLOCK aspect
                    org.cyclops.integrateddynamics.api.part.PartPos eastPartPos = org.cyclops.integrateddynamics.api.part.PartPos.of(level, eastPos, Direction.EAST);
                    PartHelpers.PartStateHolder<?, ?> eastPartStateHolder = PartHelpers.getPart(eastPartPos);
                    if (eastPartStateHolder != null) {
                        ItemStack variableCard = GameTestHelpersIntegratedDynamics.createVariableFromReader(level,
                                Aspects.Read.Redstone.BOOLEAN_CLOCK, eastPartStateHolder.getState());

                        // Place variable in writer's BOOLEAN aspect
                        org.cyclops.integrateddynamics.api.part.PartPos westPartPos = org.cyclops.integrateddynamics.api.part.PartPos.of(level, westPos, Direction.WEST);
                        GameTestHelpersIntegratedDynamics.placeVariableInWriter(null, level, westPartPos,
                                Aspects.Write.Redstone.BOOLEAN, variableCard, null);
                    }
                }
            }
        }

        /**
         * Generate a size x size x size cube of logic cables where all cables on the EAST side
         * contain redstone readers, and all cables on the WEST side contain redstone writers.
         * For each reader-writer pair at the same Y and Z coordinates, a CHOICE operator is created
         * that reads the BOOLEAN_CLOCK aspect from the reader and chooses between constants 0 and 10.
         * The result is written to the INTEGER aspect of the writer.
         * Variable cards are stored in variable store blocks placed on the SOUTH side of the network,
         * stacked vertically. Each variable store can hold multiple CHOICE operator configurations.
         * All redstone readers have PROPERTY_LENGTH set to 10.
         */
        public static void generateRedstoneNetworkVariables(ServerLevel level, BlockPos startPos, int size) {
            generateEmptyNetwork(level, startPos, size);

            List<BlockPos> updatePositions = new ArrayList<>();

            // Add redstone readers to EAST side and redstone writers to WEST side
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    // EAST side: redstone reader
                    BlockPos eastPos = startPos.offset(size - 1, y, z);
                    PartHelpers.addPart(level, eastPos, Direction.EAST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
                    updatePositions.add(eastPos);

                    // WEST side: redstone writer
                    BlockPos westPos = startPos.offset(0, y, z);
                    PartHelpers.addPart(level, westPos, Direction.WEST, PartTypes.REDSTONE_WRITER, new ItemStack(PartTypes.REDSTONE_WRITER.getItem()));
                    updatePositions.add(westPos);
                }
            }

            // Update all positions
            for (BlockPos pos : updatePositions) {
                level.updateNeighborsAt(pos, RegistryEntries.BLOCK_CABLE.value());
            }

            // Create variable stores on the SOUTH side of the network
            // Place stores at (z = size, y varying) stacked vertically
            // Each store can hold 4 items: clock variable, constant 0, constant 10, and choice operator
            int storeX = startPos.getX(); // Aligned with the network
            int storeZ = startPos.getZ() + size; // SOUTH side
            int currentStoreY = startPos.getY();
            int currentSlot = 0;
            BlockEntityVariablestore currentVariableStore = null;
            BlockPos currentStorePos = null;

            // Create variables connecting readers to writers using CHOICE operator
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    BlockPos eastPos = startPos.offset(size - 1, y, z);
                    BlockPos westPos = startPos.offset(0, y, z);

                    // Get or create a new variable store if current one is full
                    if (currentVariableStore == null || currentSlot >= BlockEntityVariablestore.INVENTORY_SIZE) {
                        if (currentSlot >= BlockEntityVariablestore.INVENTORY_SIZE) {
                            // Current store is full, move to next store (stack vertically)
                            currentStoreY++;
                        }

                        currentStorePos = new BlockPos(storeX, currentStoreY, storeZ);
                        level.setBlock(currentStorePos, RegistryEntries.BLOCK_VARIABLE_STORE.get().defaultBlockState(), 2);
                        currentVariableStore = (BlockEntityVariablestore) level.getBlockEntity(currentStorePos);
                        currentSlot = 0;
                    }

                    if (currentVariableStore != null) {
                        // Create variable from reader's BOOLEAN_CLOCK aspect
                        org.cyclops.integrateddynamics.api.part.PartPos eastPartPos = org.cyclops.integrateddynamics.api.part.PartPos.of(level, eastPos, Direction.EAST);
                        PartHelpers.PartStateHolder<?, ?> eastPartStateHolder = PartHelpers.getPart(eastPartPos);

                        if (eastPartStateHolder != null) {
                            // Create constant integer variables (0 and 10) - reuse from first slot if already created
                            ItemStack variable0, variable10;
                            int variable0Id, variable10Id;

                            int currentSlotIncrement;
                            if (currentSlot == 0) {
                                // First time, create and store constants
                                variable0 = GameTestHelpersIntegratedDynamics.createVariableForValue(level, ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(0));
                                variable10 = GameTestHelpersIntegratedDynamics.createVariableForValue(level, ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(10));
                                currentVariableStore.getInventory().setItem(1, variable0);
                                currentVariableStore.getInventory().setItem(2, variable10);
                                variable0Id = GameTestHelpersIntegratedDynamics.getVariableFacade(level, variable0).getId();
                                variable10Id = GameTestHelpersIntegratedDynamics.getVariableFacade(level, variable10).getId();
                                currentSlotIncrement = 4;
                            } else {
                                // Reuse constants from slots 1 and 2
                                variable0Id = GameTestHelpersIntegratedDynamics.getVariableFacade(level, currentVariableStore.getInventory().getItem(1)).getId();
                                variable10Id = GameTestHelpersIntegratedDynamics.getVariableFacade(level, currentVariableStore.getInventory().getItem(2)).getId();
                                currentSlotIncrement = 2;
                            }

                            // Create variable from reader's BOOLEAN_CLOCK aspect
                            ItemStack variableClock = GameTestHelpersIntegratedDynamics.createVariableFromReader(level,
                                    Aspects.Read.Redstone.BOOLEAN_CLOCK, eastPartStateHolder.getState());
                            currentVariableStore.getInventory().setItem(currentSlot, variableClock);

                            // Create CHOICE operator variable
                            ItemStack variableChoice = GameTestHelpersIntegratedDynamics.createVariableForOperator(level, Operators.GENERAL_CHOICE, new int[]{
                                    GameTestHelpersIntegratedDynamics.getVariableFacade(level, variableClock).getId(),
                                    variable0Id,
                                    variable10Id
                            });
                            currentVariableStore.getInventory().setItem(currentSlot + currentSlotIncrement - 1, variableChoice);

                            // Place CHOICE variable in writer's INTEGER aspect
                            org.cyclops.integrateddynamics.api.part.PartPos westPartPos = org.cyclops.integrateddynamics.api.part.PartPos.of(level, westPos, Direction.WEST);
                            GameTestHelpersIntegratedDynamics.placeVariableInWriter(null, level, westPartPos,
                                    Aspects.Write.Redstone.INTEGER, variableChoice, null);

                            currentSlot += currentSlotIncrement;
                        }
                    }
                }
            }

            // Set PROPERTY_LENGTH to 10 for all redstone readers
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    BlockPos eastPos = startPos.offset(size - 1, y, z);
                    PartPos eastPartPos = PartPos.of(level, eastPos, Direction.EAST);
                    GameTestHelpersIntegratedDynamics.setAspectProperty(eastPartPos, Aspects.Read.Redstone.BOOLEAN_CLOCK, AspectReadBuilders.Redstone.PROPERTY_LENGTH, ValueTypeInteger.ValueInteger.of(10));
                }
            }
        }
    }
}
