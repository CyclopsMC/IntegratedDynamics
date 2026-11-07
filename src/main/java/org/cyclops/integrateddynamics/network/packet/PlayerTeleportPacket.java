package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.command.CommandNetworkDiagnostics;

/**
 * Packet for subscribing a network update to a player.
 * @author rubensworks
 *
 */
public class PlayerTeleportPacket extends PacketCodec {

    @CodecField
    private String dimension;
    @CodecField
    private double x;
    @CodecField
    private double y;
    @CodecField
    private double z;
    @CodecField
    private float yaw;
    @CodecField
    private float pitch;

    public PlayerTeleportPacket() {

    }

    public PlayerTeleportPacket(ResourceKey<Level> dimension, double x, double y, double z, float yaw, float pitch) {
        this.dimension = dimension.location().toString();
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void actionClient(Level world, Player player) {

    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {
        // Only sent by the network diagnostics gui, which requires the same permission.
        if (!CommandNetworkDiagnostics.hasPermission(player)) {
            return;
        }
        ResourceLocation dimensionId = ResourceLocation.tryParse(this.dimension);
        if (dimensionId == null) {
            return;
        }
        ResourceKey<Level> dimensionType = ResourceKey.create(Registries.DIMENSION, dimensionId);
        if (!dimensionType.location().equals(player.level().dimension().location())) {
            ServerLevel level = ServerLifecycleHooks.getCurrentServer().getLevel(dimensionType);
            if (level == null) {
                return;
            }
            player.changeDimension(level);
        }
        player.connection.teleport(x + 0.5F, y + 0.5F, z + 0.5F, yaw, pitch);
    }

}
