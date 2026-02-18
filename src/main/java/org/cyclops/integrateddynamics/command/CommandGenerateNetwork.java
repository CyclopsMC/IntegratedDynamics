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
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypeRegistry;

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
                .requires((commandSource) -> commandSource.hasPermission(2));

        // Add the preset subcommand with optional size/radius argument
        builder.then(Commands.argument("preset", new ArgumentTypeEnum(NetworkPreset.class))
                .executes(new CommandGenerateNetworkExecutor(true, false))
                .then(Commands.argument("size", IntegerArgumentType.integer(1, 1000))
                        .executes(new CommandGenerateNetworkExecutor(true, true))));

        return builder;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        context.getSource().sendFailure(Component.literal("Please specify a preset: emptynetwork, idlenetwork, or clear")
                .withStyle(ChatFormatting.RED));
        return 0;
    }

    public enum NetworkPreset {
        EMPTYNETWORK,
        IDLENETWORK,
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
                context.getSource().sendFailure(Component.literal("Please specify a preset: emptynetwork, idlenetwork, or clear")
                        .withStyle(ChatFormatting.RED));
                return 0;
            }

            NetworkPreset preset = ArgumentTypeEnum.getValue(context, "preset", NetworkPreset.class);
            ServerLevel level = context.getSource().getLevel();
            BlockPos playerPos = BlockPos.containing(context.getSource().getPosition());
            int size = hasSize ? IntegerArgumentType.getInteger(context, "size") : getDefaultSize(preset);

            switch (preset) {
                case EMPTYNETWORK:
                    context.getSource().sendSuccess(
                            () -> Component.literal("Generating network preset: emptynetwork (size: " + size + "x" + size + "x" + size + ")")
                                    .withStyle(ChatFormatting.GREEN),
                            true);
                    generateEmptyNetwork(level, playerPos.above(2), size);
                    break;
                case IDLENETWORK:
                    context.getSource().sendSuccess(
                            () -> Component.literal("Generating network preset: idlenetwork (size: " + size + "x" + size + "x" + size + ")")
                                    .withStyle(ChatFormatting.GREEN),
                            true);
                    generateIdleNetwork(level, playerPos.above(2), size);
                    break;
                case CLEAR:
                    context.getSource().sendSuccess(
                            () -> Component.literal("Clearing cables within radius: " + size)
                                    .withStyle(ChatFormatting.GREEN),
                            true);
                    clearCables(level, playerPos, size);
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

        /**
         * Generate a size x size x size cube of only logic cables.
         */
        private void generateEmptyNetwork(ServerLevel level, BlockPos startPos, int size) {
            List<BlockPos> placedPositions = new ArrayList<>();

            // Skip expensive network initialization during bulk placement
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
                // Always reset the flag
                BlockCable.SKIP_NETWORK_INIT = false;
            }

            // Update cable connections for all placed positions
            for (BlockPos pos : placedPositions) {
                CableHelpers.updateConnectionsNeighbours(level, pos, CableHelpers.ALL_SIDES);
            }

            // Initialize the network for the entire cube
            NetworkHelpers.initNetwork(level, startPos, null);
        }

        /**
         * Generate a size x size x size cube of logic cables where all cables on the outer sides
         * contain random parts facing outwards.
         */
        private void generateIdleNetwork(ServerLevel level, BlockPos startPos, int size) {
            Random random = new Random();

            // Collect all positions that need updates
            List<BlockPos> updatePositions = new ArrayList<>();

            // First, create the cable cube
            generateEmptyNetwork(level, startPos, size);

            // Add parts to outer sides (collected for deferred updates)
            // Top face (y = size - 1)
            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    BlockPos pos = startPos.offset(x, size - 1, z);
                    addRandomPartDeferred(level, pos, Direction.UP, random, updatePositions);
                }
            }

            // Bottom face (y = 0)
            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    BlockPos pos = startPos.offset(x, 0, z);
                    addRandomPartDeferred(level, pos, Direction.DOWN, random, updatePositions);
                }
            }

            // Front face (z = 0)
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    BlockPos pos = startPos.offset(x, y, 0);
                    addRandomPartDeferred(level, pos, Direction.NORTH, random, updatePositions);
                }
            }

            // Back face (z = size - 1)
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    BlockPos pos = startPos.offset(x, y, size - 1);
                    addRandomPartDeferred(level, pos, Direction.SOUTH, random, updatePositions);
                }
            }

            // Left face (x = 0)
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    BlockPos pos = startPos.offset(0, y, z);
                    addRandomPartDeferred(level, pos, Direction.WEST, random, updatePositions);
                }
            }

            // Right face (x = size - 1)
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    BlockPos pos = startPos.offset(size - 1, y, z);
                    addRandomPartDeferred(level, pos, Direction.EAST, random, updatePositions);
                }
            }

            // Trigger all updates at once
            for (BlockPos pos : updatePositions) {
                level.blockUpdated(pos, RegistryEntries.BLOCK_CABLE.value());
            }
        }

        /**
         * Add a random part to the given position and side without triggering updates.
         * Positions are collected for deferred update triggering.
         */
        private void addRandomPartDeferred(ServerLevel level, BlockPos pos, Direction side, Random random, List<BlockPos> updatePositions) {
            // Get all available part types from the registry
            List<IPartType> partTypes = new ArrayList<>(PartTypeRegistry.getInstance().getPartTypes());

            if (partTypes.isEmpty()) {
                return;
            }

            // Choose a random part type
            IPartType partType = partTypes.get(random.nextInt(partTypes.size()));

            // Add the part
            ItemStack itemStack = new ItemStack(partType.getItem());
            PartHelpers.addPart(level, pos, side, partType, itemStack);

            // Collect position for deferred update
            updatePositions.add(pos);
        }

        /**
         * Clear all cable blocks within a radius of the given position.
         */
        private void clearCables(ServerLevel level, BlockPos centerPos, int radius) {
            int cleared = 0;
            int radiusSquared = radius * radius;

            // Skip expensive network initialization during bulk placement
            BlockCable.SKIP_NETWORK_INIT = true;

            try {
                // Iterate through all positions within the radius
                for (int x = centerPos.getX() - radius; x <= centerPos.getX() + radius; x++) {
                    for (int y = centerPos.getY() - radius; y <= centerPos.getY() + radius; y++) {
                        for (int z = centerPos.getZ() - radius; z <= centerPos.getZ() + radius; z++) {
                            BlockPos pos = new BlockPos(x, y, z);

                            // Check if position is within spherical radius
                            int dx = x - centerPos.getX();
                            int dy = y - centerPos.getY();
                            int dz = z - centerPos.getZ();
                            if (dx * dx + dy * dy + dz * dz <= radiusSquared) {
                                // Check if block is a cable
                                if (level.getBlockState(pos).getBlock() == RegistryEntries.BLOCK_CABLE.value()) {
                                    level.destroyBlock(pos, false);
                                    cleared++;
                                }
                            }
                        }
                    }
                }
            } finally {
                // Always reset the flag
                BlockCable.SKIP_NETWORK_INIT = false;
            }
        }
    }
}
