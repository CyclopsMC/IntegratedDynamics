package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.network.diagnostics.GuiNetworkDiagnostics;

/**
 * Packet for opening network diagnostics at a client.
 * @author rubensworks
 *
 */
public class NetworkDiagnosticsOpenClient extends PacketCodec {

    public NetworkDiagnosticsOpenClient() {

    }

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void actionClient(World world, PlayerEntity player) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				GuiNetworkDiagnostics.clearNetworkData();
				IntegratedDynamics._instance.getPacketHandler().sendToServer(NetworkDiagnosticsSubscribePacket.subscribe());
				GuiNetworkDiagnostics.start();
			}
		}).start();
	}

	@Override
	public void actionServer(World world, ServerPlayerEntity player) {

	}

}