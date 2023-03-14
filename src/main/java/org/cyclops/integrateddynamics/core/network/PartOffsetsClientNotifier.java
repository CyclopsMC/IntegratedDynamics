package org.cyclops.integrateddynamics.core.network;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.helper.WorldHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMultipartTicking;
import org.cyclops.integrateddynamics.network.packet.PartOffsetsDataPacket;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * A server-side handler that sends updates to clients listening to part offset updates.
 * @author rubensworks
 */
public class PartOffsetsClientNotifier {

    private static final PartOffsetsClientNotifier _INSTANCE = new PartOffsetsClientNotifier();

    private final List<UUID> players = Lists.newArrayList();

    private PartOffsetsClientNotifier() {

    }

    public static PartOffsetsClientNotifier getInstance() {
        return _INSTANCE;
    }

    protected ServerPlayer getPlayer(UUID uuid) {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
    }

    public synchronized void registerPlayer(ServerPlayer player) {
        if (!players.contains(player.getUUID())) {
            players.add(player.getUUID());
            sendPartOffsetsToPlayer(player);
        }
    }

    public synchronized void unRegisterPlayer(ServerPlayer player) {
        players.remove(player.getUUID());
    }

    public void sendPartOffsetsToPlayer(ServerPlayer player) {
        BlockPos centerPos = player.getOnPos();
        List<PartOffsetsClientNotifier.Entry> offsets = WorldHelpers.foldArea(player.getCommandSenderWorld(), GeneralConfig.partOffsetRenderDistance, centerPos, (list, world, pos) -> {
            BlockEntityHelpers.get(world, pos, BlockEntityMultipartTicking.class)
                    .ifPresent(blockEntity -> {
                        for (Map.Entry<Direction, IPartType<?, ?>> entry : blockEntity.getPartContainer().getParts().entrySet()) {
                            Direction sourceSide = entry.getKey();
                            IPartType partType = entry.getValue();
                            IPartState partState = blockEntity.getPartContainer().getPartState(entry.getKey());

                            Vec3i targetOffset = partType.getTargetOffset(partState);
                            Optional<Direction> targetSideOptional = Optional.ofNullable(partType.getTargetSideOverride(partState));
                            if (!targetOffset.equals(Vec3i.ZERO) || targetSideOptional.isPresent()) {
                                Direction targetSide = targetSideOptional.orElse(sourceSide.getOpposite());
                                targetOffset = targetOffset.offset(-sourceSide.getOpposite().getStepX(), -sourceSide.getOpposite().getStepY(), -sourceSide.getOpposite().getStepZ());
                                list.add(new PartOffsetsClientNotifier.Entry(pos, sourceSide, targetOffset, targetSide));
                            }
                        }
                    });
            return list;
        }, Lists.newArrayList());

        IntegratedDynamics._instance.getPacketHandler().sendToPlayer(new PartOffsetsDataPacket(offsets), player);
    }

    public void tick() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        for (UUID playerId : players) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player != null) {
                sendPartOffsetsToPlayer(player);
            }
        }
    }

    public static record Entry(BlockPos source, Direction sourceSide, Vec3i targetOffset, Direction targetSide) {}

}
