package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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

    public PlayerTeleportPacket(DimensionType dimension, double x, double y, double z, float yaw, float pitch) {
		this.dimension = dimension.getRegistryName().toString();
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
		DimensionType dimensionType = DimensionType.byName(new ResourceLocation(this.dimension));
		if (dimensionType != player.getServerWorld().getDimension().getType()) {
			player.changeDimension(dimensionType);
		}
		player.connection.setPlayerLocation(x + 0.5F, y + 0.5F, z + 0.5F, yaw, pitch);
	}
	
}