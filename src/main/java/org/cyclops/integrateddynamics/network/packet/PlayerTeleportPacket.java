package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.Reference;

/**
 * Packet for subscribing a network update to a player.
 * @author rubensworks
 *
 */
public class PlayerTeleportPacket extends PacketCodec {

    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "player_teleport");

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
        super(ID);
    }

    public PlayerTeleportPacket(ResourceKey<Level> dimension, double x, double y, double z, float yaw, float pitch) {
        super(ID);
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
        ResourceKey<Level> dimensionType = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(this.dimension));
        if (!dimensionType.location().equals(player.level().dimension().location())) {
            player.changeDimension(ServerLifecycleHooks.getCurrentServer().getLevel(dimensionType));
        }
        player.connection.teleport(x + 0.5F, y + 0.5F, z + 0.5F, yaw, pitch);
    }

}
