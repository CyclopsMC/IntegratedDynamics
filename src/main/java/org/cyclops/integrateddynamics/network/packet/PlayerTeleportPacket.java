package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

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

    public PlayerTeleportPacket(RegistryKey<World> dimension, double x, double y, double z, float yaw, float pitch) {
		this.dimension = dimension.getLocation().toString();
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
	public void actionClient(World world, PlayerEntity player) {

	}

	@Override
	public void actionServer(World world, ServerPlayerEntity player) {
		RegistryKey<World> dimensionType = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(this.dimension));
		if (!dimensionType.getLocation().equals(player.getServerWorld().getDimensionKey().getLocation())) {
			player.changeDimension(ServerLifecycleHooks.getCurrentServer().getWorld(dimensionType));
		}
		player.connection.setPlayerLocation(x + 0.5F, y + 0.5F, z + 0.5F, yaw, pitch);
	}
	
}