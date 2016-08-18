package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.core.network.diagnostics.NetworkDiagnostics;

/**
 * Packet for subscribing a player to network diagnostics updates.
 * @author rubensworks
 *
 */
public class NetworkDiagnosticsSubscribePacket extends PacketCodec {

    @CodecField
    private boolean subscribe;

    public NetworkDiagnosticsSubscribePacket() {

    }

    public NetworkDiagnosticsSubscribePacket(boolean subscribe) {
		this.subscribe = subscribe;
    }

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void actionClient(World world, EntityPlayer player) {
		
	}

	@Override
	public void actionServer(World world, EntityPlayerMP player) {
		if (subscribe) {
			NetworkDiagnostics.getInstance().registerPlayer(player);
		} else {
			NetworkDiagnostics.getInstance().unRegisterPlayer(player);
		}
	}

	public static NetworkDiagnosticsSubscribePacket subscribe() {
		return new NetworkDiagnosticsSubscribePacket(true);
	}

	public static NetworkDiagnosticsSubscribePacket unsubscribe() {
		return new NetworkDiagnosticsSubscribePacket(false);
	}
	
}