package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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
    public void actionClient(Level world, Player player) {
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
    public void actionServer(Level world, ServerPlayer player) {

    }

}
